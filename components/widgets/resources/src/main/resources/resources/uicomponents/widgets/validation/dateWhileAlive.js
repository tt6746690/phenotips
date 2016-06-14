var PhenoTips = (function(PhenoTips) {
  // Start PhenoTips augmentation
  var widgets = PhenoTips.widgets = PhenoTips.widgets || {};

  widgets.DateWhileAlive = Class.create({
    initialize: function(el) {
      // DOM element pointers
      this.el = el;
      this._patientDobEl = $(document.documentElement).down('input[name$=date_of_birth]');
      this._patientDodEl = $(document.documentElement).down('input[name$=date_of_death]');

      // Bind methods
      this._handleChange = this._handleChange.bind(this);
      this._isDateWhileAlive = this._isDateWhileAlive.bind(this);

      // Set up validation
      this.el.__validation = this.el.__validation || new LiveValidation(this.el, {validMessage: ''});
      this.el.__validation.add(this._isDateWhileAlive);

      // Attach handlers
      // FuzzyDatePicker components fire "xwiki:date:changed", this might need to change to work with
      // a different datepicker component.
      var eventName = "xwiki:date:changed";
      this.el.observe(eventName, this._handleChange);
      this._patientDobEl.observe(eventName, this._handleChange);
      this._patientDodEl.observe(eventName, this._handleChange);

      // Init the target DOM element
      this.el.__dateWhileAlive = this;
    },

    _isDateWhileAlive: function() {
      var bday = (this._patientDobEl && this._patientDobEl.alt) ? new Date(this._patientDobEl.alt).toUTC() : null;
      var dday = (this._patientDodEl && this._patientDodEl.alt) ? new Date(this._patientDodEl.alt).toUTC() : null;
      var thisDate = new Date(this.el.alt);

      if (bday !== null && thisDate < bday) {
        Validate.fail("$services.localization.render('phenotips.widgets.dateWhileAlive.dateBeforeBirth')");
      } else if (dday !== null && thisDate > dday) {
        Validate.fail("$services.localization.render('phenotips.widgets.dateWhileAlive.dateAfterDeath')");
      } else {
        return true;
      }
    },

    _handleChange: function() {
      this.el.__validation.validate();
    },
  });

  var init = function(event) {
    ((event && event.memo.elements) || [$('body')]).each(function(element) {
      element.select('input[type="text"].pt-date-while-alive').each(function(item) {
        if (!item.__dateWhileAlive) {
          new PhenoTips.widgets.DateWhileAlive(item);
        }
      });
    });
    return true;
  };

  (XWiki.domIsLoaded && init()) || document.observe("xwiki:dom:loaded", init);
  document.observe("xwiki:dom:updated", init);

  // End PhenoTips augmentation.
  return PhenoTips;
}(PhenoTips || {}));