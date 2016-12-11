/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
<#include "/assets/js/sso/password.js" />
<#include "/assets/js/sso/password-bad-patterns.js" />

(function() {
  $('#restorePasswordForm').submit(function() {
    $('#restorePasswordSubmit').attr('disabled', 'disabled');
    $('#restorePasswordSubmit').html('${i18n("signInButtonTitleLoading")}');
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
