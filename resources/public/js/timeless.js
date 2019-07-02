/*
 * Function to load the timeline data from the argument into the table, and
 * then update all the epoch times and differences so the table looks "good".
 */
function mkTimelineData(times) {
  var data = [['2019-04-23 08:02:48.412', null, null],
              ['April 23rd 2019, 08:02:48.696', null, null],
              ['2019-04-23 08:03:18.596', null, null],
              ['08:03:20.596', null, null],
              ['07:01:05.007', null, null],
              [null, null, null]
             ];
  return data;
}

/*
 * Function to take the Timeline data from the back-end
 * and fill out the table on the page.
 */
function buildTimelineTable(times) {
  // make the function to specify the format for the cells
  var fmts = function (row, col, prop) {
    if ((row === 0) && (col === 2)) {
      return {type: {renderer: fauxHeaderCenter},
              readOnly: true};
    } else if ((col === 1) && (row >= 0)) {
      // these are the time conversions
      return {type: 'numeric',
              format: '0,0',
              readOnly: true};
    } else if ((col === 2) && (row >= 0)) {
      // these are the time differences
      return {type: {renderer: timeDiff},
              format: '0,0',
              readOnly: true};
    } else if ((col === 1) || (col === 2)) {
      return {readOnly: true};
    } else {
      // everything in this table has to at least be read-only
      return {readOnly: false};
    }
  }
  // this is how we update the table
  var conv = function (chgs, src) {
    if (chgs) {
      $.each(chgs, function(i, info) {
        var row = info[0];
        var col = info[1];
        if (col === 0) {
          fetchEpoch(row, info[3]);
        }
      });
    }
  }
  // build up the Closing, Transaction, and Loan Information table
  var curr = mkTimelineData(null);
  $("#timelineTable").handsontable({ data: curr,
                                     colHeaders: ['Timestamp', 'Since Epoch (ms)', 'Diff (ms)'],
                                     colWidths: [70, 40, 40],
                                     columns: null,
                                     mergeCells: null,
                                     stretchH: 'all',
                                     contextMenu: {
                                       items: {
                                         "row_above": {
                                           disabled: function() {
                                             // disable this for invalid location(s)
                                             return this.getSelected()[0] < 0;
                                           }
                                         },
                                         "row_below": {
                                           disabled: function() {
                                             // disable this for invalid location(s)
                                             return false;
                                           }
                                         },
                                         "remove_row": {
                                           disabled: function() {
                                             // disable this for invalid location(s)
                                             return this.getSelected()[0] < 3;
                                           }
                                         }
                                       }
                                     },
                                     afterChange: conv,
                                     manualColumnResize: false,
                                     cells: fmts });
  // now let's update the epoch times - if they exist
  $.each(curr, function(i, row) {
    if (row[0]) {
      fetchEpoch(i, row[0]);
    }
  });
}

/*
 * Function to clear out the timestamps from the table, and then refresh
 * the table to make sure that it's all cleared out and ready to go.
 */
function wipeTable(tid) {
  var rows = $(tid).handsontable('countRows');
  for (var i = 0; i < rows; i++) {
    $(tid).handsontable('setDataAtCell', i, 0, '');
    $(tid).handsontable('setDataAtCell', i, 1, '');
  }
}

