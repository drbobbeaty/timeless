/*
 * This is a simple regex-based email validator that should work on the vast
 * majority of the cases that we run into. It's hard-coded the top-level
 * domains, but that's not really too surprising.
 */
function validEmail(email) {
  if ((email === undefined) || (email == '')) {
    return false;
  } else {
    var re = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
    var em = email.trim();
    return re.test(em);
  }
}
