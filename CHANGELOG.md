## v0.5.0 / 2019 Apr 27

> This release adds a `clear` button to the UI so that the table can be cleared
> of all the data easily, and then new data can flow in without worrying about
> leaving bad stuff around in the cells. This was just another request, and it
> was nice to dig into the guts of the HandsOnTable again. Good stuff.

* **Add** - added the `wipeTable()` function, and tied it to the button on the
  UI.

```clojure
[timeless "0.5.0"]
```

## v0.4.0 / 2019 Apr 26

> This release adds the ability to parse naked times and infer the dates from
> the data in the table - assuming 'today', if we have nothing concrete. At the
> same time, we assumed that the times are in _ascending_ order, so that we
> look at the times, and if the dates are missing, we add a day to the latter
> one to keep them in order.

* **Add** - added the scanning for the date when dealing with naked times
* **Add** - added the day offset for naked times where the time is after the
  time in the previous row.
* **Fix** - cleaned up the `deploy` directory - it's unnecessary for Heroku

```clojure
[timeless "0.4.0"]
```

## v0.3.0 / 2019 Apr 25

> This release adds a new timestamp formatter to the mix so that we cover
> yet another base for the users. This required that we add sttrict encoding
> of the argument in the URL on the call, but that's just being smart anyway.

* **Add** - added the `MMMM dd yyyy, HH:mm:ss.SSS` format to the formatter list
* **Add** - added the stripping of `st`, `nd`, `rd`, and `th` on the end of the
  date in the timestamp so that we don't have to hassle with it on the parsing.
* **Fix** - added the URI encoding to the argument on the call to the service.
* **Update** - removed some nasty whitespace from the bad tab settings.

```clojure
[timeless "0.3.0"]
```

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
