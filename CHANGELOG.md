## v0.1.1 / 2019 Apr 23

> This release fixes two issues with the app, one is in trying to parse the
> `undefined` value coming from the Javascript client, and the other is the
> pop-up menu for the adding and removing of rows.

* **Add** - added the test on the `undefined` input from the caller, and just
  made it do that we return a zero for that guy. Much cleaner.
* **Fix** - fixed the disabled rules on the pop-up menu on the table for adding
  and removing rows in the table.

```clojure
[timeless "0.1.1"]
```

## v0.1.0 / 2019 Apr 23

> This is the initial release of the `timeless` service and app.

* **New** - everything - it's brand new

```clojure
[timeless "0.1.0"]
```
