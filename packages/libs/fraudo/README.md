# Fraudo DSL

Language for describing antifraud patterns

Provides the ability to describe the required set of rules for characteristics
and triggers for attempts at fraudulent actions

**Documentation** - https://valitydev.github.io/fraudo/

**Language use antlr4, developers can use this documentation** - https://www.antlr.org/

#### How to build docs locally

From the monorepo root:

```bash
pip install -r docs/fraudo/requirements.txt mkdocs-gitbook
python3 -m mkdocs serve -f docs/fraudo/mkdocs.yml
```
