package com.example.bhandara.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.network.ApiResult
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.photos.CompressedPhoto
import com.example.bhandara.photos.PickedPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @param menuPhotos      photos of the menu, for AI to read
 * @param scanFound       how many items the last scan added, to say so
 * @param error           what went wrong, ready to show
 * @param savedItems      once saved: what was saved (the screen closes)
 */
data class AddMenuItemsState(
    val rows: List<MenuRow>,
    val menuPhotos: List<PickedPhoto> = emptyList(),
    val scanning: Boolean = false,
    val scanFound: Int? = null,
    val saving: Boolean = false,
    val error: String? = null,
    val savedItems: List<MenuItemRequest>? = null,
) {
    val busy: Boolean get() = scanning || saving
    val canAddMenuPhotos: Boolean get() = menuPhotos.size < MAX_MENU_PHOTOS
}

/**
 * Adding items to a menu, the same on Android and iOS: typed in, or read from photos of the menu by AI and then
 * checked by the user. Saves to the owner's shop when [shopId] is given; otherwise it's a draft for a shop being
 * added, and the items are just handed back.
 *
 * @param existingNames items already on the menu, which can't be added again
 * @param initialItems  items from an earlier draft
 */
class AddMenuItemsViewModel(
    private val shopId: String?,
    private val existingNames: List<String>,
    initialItems: List<MenuItemRequest>,
    private val menuApi: MenuApi,
) : ViewModel() {

    private var nextId = 0L

    private val _state = MutableStateFlow(
        AddMenuItemsState(rows = initialItems.map { it.toRow() }.ifEmpty { listOf(newRow()) })
    )
    val state: StateFlow<AddMenuItemsState> = _state.asStateFlow()

    // ==================== Typing items ====================

    fun addRow() = edit { it.copy(rows = it.rows + newRow()) }

    fun removeRow(id: Long) = edit { state ->
        // Always one row to type in
        state.copy(rows = (state.rows.filterNot { it.id == id }).ifEmpty { listOf(newRow()) })
    }

    fun onNameChange(id: Long, name: String) = editRow(id) { it.copy(name = name.take(MAX_MENU_NAME)) }

    fun onPriceChange(id: Long, price: String) = editRow(id) { it.copy(price = cleanPrice(price)) }

    fun onFoodTypeChange(id: Long, foodType: String) = editRow(id) { it.copy(foodType = foodType) }

    /** Starts the menu over (the add-shop draft's "Clear menu") */
    fun clearAll() = edit { it.copy(rows = listOf(newRow()), menuPhotos = emptyList(), scanFound = null) }

    // ==================== Reading a menu photo ====================

    fun addMenuPhotos(photos: List<PickedPhoto>) =
        edit { it.copy(menuPhotos = it.menuPhotos + photos.take(MAX_MENU_PHOTOS - it.menuPhotos.size)) }

    fun removeMenuPhoto(photo: PickedPhoto) = edit { it.copy(menuPhotos = it.menuPhotos - photo) }

    /**
     * Sends the menu photos to AI and adds what it reads as rows for the user to check (in place of an untouched
     * blank row). Items already listed or on the menu aren't added again.
     */
    fun scan() {
        val state = _state.value
        if (state.busy || state.menuPhotos.isEmpty()) return
        _state.update { it.copy(scanning = true, error = null, scanFound = null) }
        viewModelScope.launch {
            val compressed = mutableListOf<CompressedPhoto>()
            for (photo in state.menuPhotos) {
                compressed += photo.compressed() ?: return@launch failScan("Couldn't read one of the photos. Try another photo.")
            }
            when (val result = menuApi.readMenuPhotos(compressed)) {
                is ApiResult.Success -> {
                    val known = (existingNames + _state.value.rows.map { it.name }).map { it.trim().lowercase() }.toMutableSet()
                    val found = result.value.filter { it.name.isNotBlank() && known.add(it.name.trim().lowercase()) }
                    if (found.isEmpty()) {
                        return@launch failScan(
                            if (result.value.isEmpty()) "No menu items could be read from these photos. Try clearer photos."
                            else "Everything in these photos is already listed."
                        )
                    }
                    _state.update {
                        it.copy(
                            rows = it.rows.filterNot { row -> row.isBlank } + found.map { item -> item.toRow() },
                            menuPhotos = emptyList(),
                            scanning = false,
                            scanFound = found.size,
                        )
                    }
                }
                is ApiResult.Failure -> failScan(result.error.message)
            }
        }
    }

    // ==================== Saving ====================

    /** Checks the items, then saves them to the shop (or hands back the draft) */
    fun save() {
        val state = _state.value
        if (state.busy || state.savedItems != null) return
        val items = menuItemsFrom(state.rows, existingNames).getOrElse { problem ->
            _state.update { it.copy(error = problem.message) }
            return
        }
        if (shopId == null) {
            _state.update { it.copy(savedItems = items, error = null) }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = menuApi.addItems(shopId, items)) {
                is ApiResult.Success -> _state.update { it.copy(saving = false, savedItems = items) }
                is ApiResult.Failure -> _state.update { it.copy(saving = false, error = result.error.message) }
            }
        }
    }

    fun onErrorShown() = _state.update { it.copy(error = null) }

    private fun failScan(message: String) = _state.update { it.copy(scanning = false, error = message) }

    private fun newRow() = MenuRow(id = nextId++)

    private fun MenuItemRequest.toRow() = MenuRow(id = nextId++, name = name, price = priceText(price), foodType = foodType)

    private fun edit(change: (AddMenuItemsState) -> AddMenuItemsState) {
        if (_state.value.busy) return
        _state.update { change(it).copy(error = null) }
    }

    private fun editRow(id: Long, change: (MenuRow) -> MenuRow) =
        edit { state -> state.copy(rows = state.rows.map { if (it.id == id) change(it) else it }) }
}
