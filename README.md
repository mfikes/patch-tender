# patch-tender

Check that each ClojureScript patch in `resources/patches.txt` applies:

```
plk -Acheck
```

Build ClojureScript compiler with patches for select tickets in `resources/tickets.txt`:

```
plk -Abuild
```
