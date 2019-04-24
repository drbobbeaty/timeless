## v0.2.1 / 2019 Apr 24

> This release fixes up the pulling of the port number from Heroku's
> environment. This was pretty important, as it has to know where to look for
> the service, and we can't use _just any_ port. This should help, I think.

* **Add** - added in the `env` support for Heroku, and pulled the port number

```clojure
[timeless "0.2.1"]
```

## v0.2.0 / 2019 Apr 24

> This release adds everything we need to deploy the app to Heroku on their
> free platform. This will make it a lot easier to let folks see the work,
> and at the same time, this is a great way to get a project up there as a
> template for other projects.

* **Add** - added all the Heroku specifics to the code

```clojure
[timeless "0.2.0"]
```

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
