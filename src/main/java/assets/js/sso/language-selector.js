var sso = sso || {};
sso.htmlHelpers = sso.htmlHelpers || {};

/**
 * <#--
 * Processes new language selection and reloads the page.
 * @param {!string} selectedLanguage New selected language.
 * @return {!boolean} Always returns false.
 * -->
 */
sso.htmlHelpers.changeLanguage = function(selectedLanguage) {
  var newUrl;
  var currentUrl = window.location.href.toString();
  var langParam = 'lang=' + encodeURIComponent(selectedLanguage);
  var langIndex = currentUrl.indexOf('lang=');
  if (langIndex < 0) {
    newUrl = currentUrl.indexOf('?') > 0 ? currentUrl + '&' : currentUrl + '?';
    newUrl += langParam;
  } else {
    newUrl = currentUrl.replace(/lang=[a-z_\-]*/ig, langParam)
  }
  window.location.href = newUrl;
  return false;
};