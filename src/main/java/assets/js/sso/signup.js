/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
<#include "/assets/js/sso/setup-dropdown.js" />
<#include "/assets/js/sso/password.js" />
<#include "/assets/js/sso/password-bad-patterns.js" />

(function() {
  /**<#-- Birthday month, gender, country. -->*/
  var setUpDropdown = sso.htmlHelpers.setUpDropdown;
  setUpDropdown('birthMonth');
  setUpDropdown('gender');
  setUpDropdown('countryId', function(li) {
    var phone = $('#phone');
    if (phone.val().length < 5) {
      phone.val('+' + li.attr('rel2') + ' ');
      phone[0].focus();
      phone[0].selectionStart = phone[0].selectionEnd = phone.val().length;
    }
    return li.attr('rel3');
  });

  $('#signUpForm').submit(function() {
    $('#signUpSubmit').attr('disabled', 'disabled');
    $('#signUpSubmit').html('${i18n("signInButtonTitleLoading")}');
  });

  var passwordObserver = new sso.security.password.Observer('#password', '#password-repeat', [
    '${i18n("signUpPasswordStrong")}',
    '${i18n("signUpPasswordMedium")}',
    '${i18n("signUpPasswordWeakRepeatedCharacters")}',
    '${i18n("signUpPasswordWeakSubsequentCharacters")}',
    '${i18n("signUpPasswordWeakWellKnownPattern")}',
    '${i18n("signUpPasswordWeakTooShort")}',
    '${i18n("signUpPasswordWeakTooFewUniqueCharacters")}'
  ]);
 })();
