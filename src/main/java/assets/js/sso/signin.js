/**<#-- This javascript source is the Freemarker template and supposed to be included. -->*/
(function() {
    var signIn = {
        key: 'signIn.rememberMe',
        button: '#signInSubmit',
        input: '#emailOrUsername',
        checkbox: '#rememberMe'
    };

    signIn.disableSubmitButton = function() {
        $(this.button).attr('disabled', 'disabled');
        $(this.button).html('${i18n("signInButtonTitleLoading")}');
    }

    signIn.saveEmailOrUsername = function() {
       var value = $(this.input).val();
       if ($(this.checkbox).is(':checked') && value) {
           value = btoa(encodeURIComponent('sde' + value));
           localStorage.setItem(this.key, value);
        } else {
           localStorage.removeItem(this.key);
        }
    }

    signIn.restoreEmailOrUsername = function() {
        var savedValue = localStorage.getItem(this.key);
        if (savedValue) {
            var value = decodeURIComponent(atob(savedValue)).substring(3);
            if (!$(this.input).val()) {
                $(this.input).val(value);
            }
            $(this.checkbox).prop('checked', true);
        }
    }

    $('#signInForm').submit(function() {
        signIn.disableSubmitButton();
        signIn.saveEmailOrUsername();
    });

    signIn.restoreEmailOrUsername();
})();