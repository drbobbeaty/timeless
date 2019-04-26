/*
 * Function to load the timeline from the back-end and put it in a
 * nice looking table on the page. Or, for now, show a blank one.
 */
function loadTimeline(email) {
  // now let's show it to the user - just in case it was hidden
  $("#mr_big").show();
  // ...and build it up
  buildTimelineTable(null);
}

/*
 * Function to simply refresh the Timeline based on the selection from
 * a manager. This is very simple, but we needed a landing spot that looked
 * the state of the select and used that for the refresh.
 */
function refreshTimeline() {
  loadTimeline($("#reviewers").val());
}

/*
 * Simple function to try and get the correct date for timestamps with just
 * a time component, and we need to see if we can find a date in the previous
 * rows. This is just a little recursive code to walk up the table until we
 * happen to get to a date - or the top of the table, where we assume 'today'.
 */
function ensureDate(row, arg) {
  if ((typeof row !== 'undefined') && (typeof arg !== 'undefined')) {
    // make sure it has a date on it...
    if (arg.match(/^\w*\d{2}:\d{2}:\d{2}/)) {
      if (row === 0) {
        // no dates to base this on... so assume today as the starting date
        arg = moment().format('YYYY-MM-dd') + ' ' + arg;
      } else {
        // pick the date off the previous msec since epoch
        var pts = $("#timelineTable").handsontable('getDataAtCell', row-1, 0);
        var hits = pts.match(/^(.+)\w*\d{2}:\d{2}:\d{2}/);
        if (hits && hits.length > 1) {
          arg = hits[1] + arg;
        } else {
          // check the previous row for a date...
          arg = ensureDate(row-1, arg);
        }
      }
    }
  }
  return arg;
}

/*
 * Function to look at the results of the parsing, and see if the current
 * row has no date, and if the previous row has a time *later* than this
 * row, then we're looking at adding a day to it to keep them all in the
 * proper chronological order.
 */
function checkSequence(row, arg, epochMsec) {
  if ((typeof row !== 'undefined') && (typeof arg !== 'undefined') &&
      (typeof epochMsec !== 'undefined')) {
    // only mess with this if the date is missing...
    if (arg.match(/^\w*\d{2}:\d{2}:\d{2}/) && (row > 0)) {
      var pts = $("#timelineTable").handsontable('getDataAtCell', row-1, 0);
      var hits = pts.match(/^(.*)\w*(\d{2}:\d{2}:\d{2}(\.\d{3})?)/);
      if (hits && hits.length > 2) {
        if (arg < hits[2]) {
          epochMsec += 86400000;    // add a day of msec to the timestamp
        }
      }
    }
  }
  return epochMsec;
}

/*
 * Simple function to look up the provided msec since epoch from the service
 * and place it in the table on the provided row. This is a very useful thing
 * to have.
 */
function fetchEpoch(row, arg) {
  if ((typeof row !== 'undefined') && (typeof arg !== 'undefined')) {
    // make the call to get the attachments to this loan
    $.ajax({type: "GET",
            cache: false,
            url: "/v1/epochize/" + encodeURIComponent(ensureDate(row, arg)),
            dataType: "json",
            success: function(data) {
              // show the docs all the time - we may add to them
              if (data) {
                $("#timelineTable").handsontable('setDataAtCell', row, 1, checkSequence(row, arg, data.epochMsec));
              }
            }
    });
  }
}