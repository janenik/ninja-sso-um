(function() {
    $('#signInForm').submit(function() {
        $('#signInSubmit').attr('disabled', 'disabled');
        $('#signInSubmit').html('${i18n("signInButtonTitleLoading")}');
    });
})();