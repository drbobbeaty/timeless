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
 * Simple function to look up the provided msec since epoch from the service
 * and place it in the table on the provided row. This is a very useful thing
 * to have.
 */
function fetchEpoch(row, arg) {
  if ((typeof row !== 'undefined') && (typeof arg !== 'undefined')) {
    // make the call to get the attachments to this loan
    $.ajax({type: "GET",
            cache: false,
            url: "/v1/epochize/" + encodeURIComponent(arg),
            dataType: "json",
            success: function(data) {
              // show the docs all the time - we may add to them
              if (data) {
                $("#timelineTable").handsontable('setDataAtCell', row, 1, data.epochMsec);
              }
            }
    });
  }
}