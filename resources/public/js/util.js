/*
 * This function extracts the provided query string from the URL for
 * the page we're on, and it's a nice and simple way to get the parts
 * of the URL that we're looking to see if they provided.
 */
function qs(key) {
    key = key.replace(/[*+?^$.\[\]{}()|\\\/]/g, "\\$&"); // escape RegEx meta chars
    var match = location.search.match(new RegExp("[?&]"+key+"=([^&]+)(&|$)"));
    return match && decodeURIComponent(match[1].replace(/\+/g, " "));
}

/*
 * Simple function to format a number as USD currency. This is needed for
 * the presentation of the previous values in the tooltips.
 */
function formatCurrency(total) {
  if (total) {
    var neg = false;
    if (total < 0) {
      neg = true;
      total = Math.abs(total);
    }
    return (neg ? "-$" : '$') + parseFloat(total, 10).toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, "$1,").toString();
  }
  return '';
}

/*
 * Simple function to format a number as USD currency with 4-DP. This is needed
 * for the presentation of the previous values in the tooltips.
 */
function formatCurrency4DP(total) {
  if (total) {
    var neg = false;
    if (total < 0) {
      neg = true;
      total = Math.abs(total);
    }
    return (neg ? "-$" : '$') + parseFloat(total, 10).toFixed(4).replace(/(\d)(?=(\d{3})+\.)/g, "$1,").toString();
  }
  return '';
}

/*
 * Simple function to format a number as a percentage. This is needed for
 * the presentation of the previous values in the tooltips.
 */
function formatPercentage(arg) {
  if (arg) {
    return parseFloat(arg, 10).toFixed(2).toString()+'%';
  }
  return '';
}

/*
 * Function to sort the date/times in an Bootstrap Table so that the times are
 * in AM/PM, but the sorting is in wall clock time.
 */
function dateTimeCompare(a, b) {
  var asec = moment(a, 'YYYY-MM-DD h:mm a').unix();
  var bsec = moment(b, 'YYYY-MM-DD h:mm a').unix();
  if (asec < bsec) return -1;
  if (asec > bsec) return 1;
  return 0;
}

/*
 * detect IE
 * returns version of IE or false, if browser is not Internet Explorer
 */
function detectIE() {
  var ua = window.navigator.userAgent;

  var msie = ua.indexOf('MSIE ');
  if (msie > 0) {
    // IE 10 or older => return version number
    return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
  }

  var trident = ua.indexOf('Trident/');
  if (trident > 0) {
    // IE 11 => return version number
    var rv = ua.indexOf('rv:');
    return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
  }

  var edge = ua.indexOf('Edge/');
  if (edge > 0) {
   // IE 12 => return version number
   return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
  }

  // other browser
  return false;
}

/*
 * Function to remove all the keys from the provided map where the value is
 * null. This is just an easy way to 'thin out' the data before moving it.
 */
function removeNullKeys(m) {
  for (var key in m) {
    var val = m[key];
    if (val == null) {
      delete m[key];
    }
  }
  return m;
}

/*
 * Sweet little function to export a Handsontable's data to a named CSV file
 * and then download it to the client - all in client-side avascript. This
 * makes exporting any such table very easy - just give it a filename and the
 * id tag of the table, and it's gone.
 */
function exportToCSV(filename, tid) {
  var processRow = function (row) {
    var finalVal = '';
      for (var j = 0; j < row.length; j++) {
        var innerValue = row[j] == null ? '' : row[j].toString();
        if (row[j] instanceof Date) {
          innerValue = row[j].toLocaleString();
        };
        var result = innerValue.replace(/"/g, '""');
        if (result.search(/("|,|\n)/g) >= 0)
          result = '"' + result + '"';
        if (j > 0)
          finalVal += ',';
        finalVal += result;
      }
      return finalVal + '\n';
  };

  var csvFile = '';
  var rows = $(tid).handsontable('getData');
  for (var i = 0; i < rows.length; i++) {
    csvFile += processRow(rows[i]);
  }

  var blob = new Blob([csvFile], { type: 'text/csv;charset=utf-8;' });
  if (navigator.msSaveBlob) { // IE 10+
    navigator.msSaveBlob(blob, filename);
  } else {
    var link = document.createElement("a");
    if (link.download !== undefined) { // feature detection
      // Browsers that support HTML5 download attribute
      var url = URL.createObjectURL(blob);
      link.setAttribute("href", url);
      link.setAttribute("download", filename);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }
}

/*
 * Renderers to make a cell _appear_ to be a header cell - same style, but
 * non-editable, and with the right background. There are two - one for
 * the centered faux header, and another for a left-justified faux header.
 */
var fauxHeaderCenter = function(instance, td, row, col, prop, value, cellProperties) {
  Handsontable.TextCell.renderer.apply(this, arguments);
  var style = td.style;
  style.textAlign = 'center';
  style.fontStyle = 'normal';
  style.fontWeight = 'bold';
  style.color = '#000';
  style.background = '#eee';
  return td;
}

var fauxHeaderLeft = function(instance, td, row, col, prop, value, cellProperties) {
  Handsontable.TextCell.renderer.apply(this, arguments);
  var style = td.style;
  style.textAlign = 'left';
  style.fontStyle = 'normal';
  style.fontWeight = 'bold';
  style.color = '#000';
  style.background = '#eee';
  return td;
}

var fauxHeaderRight = function(instance, td, row, col, prop, value, cellProperties) {
  Handsontable.TextCell.renderer.apply(this, arguments);
  var style = td.style;
  style.textAlign = 'right';
  style.fontStyle = 'normal';
  style.fontWeight = 'bold';
  style.color = '#000';
  style.background = '#eee';
  return td;
}

/*
 * There are also gells in the tables that are fixtures - elements that can't
 * change, and aren't editable by anyone, but they aren't headers. So do just
 * about the same thing as the faux headers, but skip the background color.
 */
var fixtureCenter = function(instance, td, row, col, prop, value, cellProperties) {
  Handsontable.TextCell.renderer.apply(this, arguments);
  var style = td.style;
  style.textAlign = 'center';
  style.fontStyle = 'normal';
  style.color = '#000';
  return td;
}

var fixtureLeft = function(instance, td, row, col, prop, value, cellProperties) {
  Handsontable.TextCell.renderer.apply(this, arguments);
  var style = td.style;
  style.textAlign = 'left';
  style.fontStyle = 'normal';
  style.color = '#000';
  return td;
}

var fixtureRight = function(instance, td, row, col, prop, value, cellProperties) {
  Handsontable.TextCell.renderer.apply(this, arguments);
  var style = td.style;
  style.textAlign = 'right';
  style.fontStyle = 'normal';
  style.color = '#000';
  return td;
}

/*
 * There are a few headers that need a white background, double-height, with
 * the label bold in the center. This is just the way they wanted it done.
 */
var dblHeightCenter = function(instance, td, row, col, prop, value, cellProperties) {
  Handsontable.TextCell.renderer.apply(this, arguments);
  var style = td.style;
  style.textAlign = 'center';
  style.verticalAlign = 'middle';
  style.fontStyle = 'normal';
  style.fontWeight = 'bold';
  style.color = '#000';
  style.height = '50px';
  return td;
}

/*
 * The Timeline table needs to have a computed 'diff' for the current line
 * to the revious line. These are all msec times, so it's just a difference
 * of two numbers. Shouldn't be hard. :)
 */
var timeDiff = function(instance, td, row, col, prop, value) {
  if (instance.getDataAtCell(row, 0)) {
    var a = instance.getDataAtCell(row, 1);
    var b = instance.getDataAtCell((row - 1), 1);
    value = ($.isNumeric(a) ? a : 0) - ($.isNumeric(b) ? b : 0);
  } else {
    value = null;
  }
  Handsontable.NumericRenderer.apply(this, arguments);
}
