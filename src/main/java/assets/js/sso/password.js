/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
var sso = sso || {};
sso.security = sso.security || {};
sso.security.password = sso.security.password || {};

/**<#--
 * Creates observer for two password fields and shows appropriate evaluation of the
 * entered passwords.
 * @param inputId Password input id. Must start with '#'.
 * @param repeatInputId Password repeat input id. Must start with '#'.
 * @param meterMessages Password strength meter messages.
-->*/
sso.security.password.Observer = function(inputId, repeatInputId, meterMessages) {
  this.input = $(inputId);
  this.repeatInput = $(repeatInputId);
  this.inputFeedback = $(inputId + '-feedback');
  this.repeatInputFeedback = $(repeatInputId + '-feedback');
  this.meter = $(inputId + '-meter');

  this.meterMessages = meterMessages || [];

  this.ok = 'glyphicon-ok-circle';
  this.not = 'glyphicon-remove-circle';

  this.input.on("input", $.proxy(this.onFieldChange.bind(this)));
  this.repeatInput.on("input", $.proxy(this.onRepeatFieldChange.bind(this)));

  this.classifier = new sso.security.password.Classifier();
}

/**<#--
 * Observes main password field and shows appropriate evaluation of the entered
 * password.
-->*/
sso.security.password.Observer.prototype.onFieldChange = function() {
  var value = this.input.val();
  if (value == '') {
    this.meter.hide();
    this.inputFeedback.hide();
    return;
  }
  this.meter.css('display', 'inline-block');
  var verdict = this.classifier.classify(value);
  text = this.meterMessages[verdict - 1] || '?No translation for verdict: ' + (verdict);
  this.meter.text(text);
  if (verdict > 2) {
    return this.meter.removeClass('strong medium').addClass('weak');
  } else if (verdict == 2) {
    this.meter.removeClass('strong weak').addClass('medium');
  } else {
    this.meter.removeClass('weak medium').addClass('strong');
  }
  this.inputFeedback.show();
  this.input.closest('.has-error')
      .removeClass('has-error')
      .find('p.help-block').hide();
}

/**<#--
 * Observes repeat password field and shows appropriate evaluation of the entered
 * password.
-->*/
sso.security.password.Observer.prototype.onRepeatFieldChange = function() {
  var repeat = this.repeatInput.val();
  var same = this.input.val() == repeat;
  this.updateFeedback(this.repeatInputFeedback, repeat != '', same);
  if (same) {
    this.repeatInput.closest('.has-error')
        .removeClass('has-error')
        .find('p.help-block').hide();
  }
}

/**<#--
 * Updates feedback field.
 * @param f Feedback jquery element.
 * @param show Whether to show the element.
 * @param isGood Whether the password state is good.
-->*/
sso.security.password.Observer.prototype.updateFeedback = function(f, show, isGood) {
  show ? f.show() : f.hide();
  isGood ? f.removeClass(this.not).addClass(this.ok) : f.removeClass(this.ok).addClass(this.not);
}

/**<#--
 * Enumeration for all password classifier values. Medium and strong are accepted values.
-->*/

sso.security.password.Verdict = {
  STRONG: 1,
  MEDIUM: 2,
  WEAK_REPEATED_CHARACTERS: 3,
  WEAK_SUBSEQUENT_CHARACTERS: 4,
  WEAK_WELL_KNOWN: 5,
  WEAK_TOO_SHORT: 6,
  WEAK_TOO_FEW_UNIQUE_CHARACTERS: 7
}

/**<#--
 * Password classifier.
 * @param opt_badPatterns Optional bad patterns array.
-->*/
sso.security.password.Classifier = function(opt_badPatterns) {
  this.minLength = 8;
  this.sameThreshold = 3;
  this.increasingThreshold = 3;
  this.decreasingThreshold = 3;
  this.uniquenessThreshold = 0.3;
  this.wellKnownPartThreshold = 0.4;
  this.badPatterns = opt_badPatterns || sso.security.password.DefaultBadPatterns;
  this.specialCharacters = {};
  var specialArray = '!@#$%^&*()_+-=~`[];\':"<>/?|\\'.split('');
  for (var i = 0; i < specialArray.length; i++) {
    this.specialCharacters[specialArray[i]] = 1;
  }
}

/**<#--
 * Classifies password strength.
 * @param password Password to classify.
 * @return Verdict with one of the enumeration values.
-->*/
sso.security.password.Classifier.prototype.classify = function(password) {
  var Verdict = sso.security.password.Verdict;
  var i, increasing = 1, decreasing = 1, same = 1, previous = null, current, currentCode;
  var passwordLow, passwordLowReversed, wellKnownRemainder;

  if (!password || !password.length || password.length < this.minLength) {
    return Verdict.WEAK_TOO_SHORT;
  }

  var histo = {}, digits, lower, upper, special;
  for (i = 0; i < password.length; i++) {
    current = password.charAt(i);
    if (!upper && current != current.toLocaleLowerCase()) upper = true;
    if (!lower && current != current.toLocaleUpperCase()) lower = true;
    if (!digits && current >= '0' && current <= '9') digits = true;
    if (!special && this.specialCharacters[current]) special = true;
    currentCode = password.charCodeAt(i);
    if (currentCode == previous) {
      same++;
      if (same == this.sameThreshold) {
        return Verdict.WEAK_REPEATED_CHARACTERS;
      }
    } else {
      same = 1;
    }
    if (currentCode == previous + 1) {
      increasing++;
      if (increasing == this.increasingThreshold) {
        return Verdict.WEAK_SUBSEQUENT_CHARACTERS;
      }
    } else {
      increasing = 1;
    }
    if (currentCode == previous - 1) {
      decreasing++;
      if (decreasing == this.decreasingThreshold) {
        return Verdict.WEAK_SUBSEQUENT_CHARACTERS;
      }
    } else {
      decreasing = 1;
    }
    histo[current] = (histo[current] || 0) + 1;
    previous = currentCode;
  }
  if (Object.keys(histo).length / password.length <= this.uniquenessThreshold) {
    return Verdict.WEAK_TOO_FEW_UNIQUE_CHARACTERS;
  }

  passwordLow = password.toLocaleLowerCase();
  passwordLowReversed = passwordLow.split('').reverse().join('');

  for (i = 0; i < this.badPatterns.length; i++) {
    current = this.badPatterns[i];
    wellKnownRemainder = null;
    if (passwordLow.indexOf(current) >= 0) {
      wellKnownRemainder = passwordLow.split(current).join('');
    }
    else if(passwordLowReversed.indexOf(current) >= 0) {
      wellKnownRemainder = passwordLowReversed.split(current).join('');
    }
    if (wellKnownRemainder != null
      && wellKnownRemainder.length / password.length <= this.wellKnownPartThreshold) {
      return Verdict.WEAK_WELL_KNOWN;
    }
  }
  if (upper && lower && (special || digits)) {
    return Verdict.STRONG;
  }
  return Verdict.MEDIUM;
}