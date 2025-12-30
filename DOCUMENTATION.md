# Documentation Guide

This project uses [MkDocs](https://www.mkdocs.org/) with the [Material theme](https://squidfunk.github.io/mkdocs-material/) for documentation.

## 📖 Live Documentation

**https://piyushchouhan.github.io/bhandara/**

## 🚀 Quick Start

### Preview Locally

```bash
source venv/bin/activate
mkdocs serve
```

Then open http://127.0.0.1:8000 in your browser. Changes auto-reload!

### Deploy to GitHub Pages

```bash
source venv/bin/activate
mkdocs gh-deploy
```

Documentation will be live at https://piyushchouhan.github.io/bhandara/ in ~1 minute.

## 📝 Editing Documentation

1. Edit markdown files in the `docs/` folder
2. Work on the `main` branch (never edit `gh-pages` manually)
3. Preview changes with `mkdocs serve`
4. Deploy with `mkdocs gh-deploy`

## 📂 Documentation Structure

```
docs/
├── index.md           # Home page
├── getting-started.md # Setup guide
├── architecture.md    # Architecture overview
├── features.md        # Features documentation
├── development.md     # Development guidelines
└── api-reference.md   # API documentation
```

## ⚙️ Configuration

- `mkdocs.yml` - MkDocs configuration
- `docs/` - Documentation source files
- `venv/` - Python virtual environment (gitignored)
- `site/` - Built documentation (gitignored, auto-generated)

## 🔧 One-Time Setup (Already Done)

```bash
python3 -m venv venv
source venv/bin/activate
pip install mkdocs-material
mkdocs new .
```

## 📌 Notes

- The `gh-pages` branch is automatically created and managed by `mkdocs gh-deploy`
- Never manually edit the `gh-pages` branch
- Always activate the virtual environment before running mkdocs commands
