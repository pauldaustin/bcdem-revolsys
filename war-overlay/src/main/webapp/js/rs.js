function resizeHeight(iframe) {
  var body = $(iframe, window.top.document).contents().find('body');
  var newHeight = body.height() + 5;

  $(iframe).height(newHeight);
}

(function($) {
  $.fn.iframeAutoHeight = function() {
    var result = this.each(function() {
      $(this).load(function() {
        resizeHeight(this);
      });
      var source = $(this).attr('src');
      $(this).attr('src', '');
      $(this).attr('src', source);
    });
    return result;
  };
}(jQuery));

function addConfirmButton(params) {
  rsConfirmButtons.push(params);
}

function confirmButton(root, params) {
  var selector = params['selector'];
  var icon = params['icon'];
  var title = params['title'];
  var message = params['message'];

  $(selector, root).button({
    text : false,
    icons : {
      primary : 'ui-icon-' + icon
    }
  });
  $(selector, root).click(function() {
    var form = $(this).closest('form');
    $('<div></div>').html(message).dialog({
      title : title,
      buttons : {
        'Cancel' : function() {
          $(this).dialog('close');
        },
        'OK' : function() {
          $(this).dialog('close');
          $('<input>').attr({
            type : 'hidden',
            name : 'confirm',
            value : 'true'
          }).appendTo(form);
          form.submit();
        }
      }
    });
    return false;
  });

}
var rsConfirmButtons = new Array();

function refreshButtons(root) {
  $(rsConfirmButtons).each(function() {
    confirmButton(root, this);
  });

  $('div.actionMenu a', root).button();

  $('.button', root).button();

  $(':button,:submit', root).each(function() {
    var button = $(this);
    var classes = button.attr('class');
    var icon = undefined;
    if (classes != undefined) {
      $(classes.split(/\s+/)).each(function() {
        var cssClass = this;
        if (cssClass.indexOf("ui-auto-button-") == 0) {
          icon = cssClass.substring("ui-auto-button-".length);
        }
      });
    }
    if (icon != undefined) {
      button.button({
        icons : {
          primary : "ui-icon-" + icon
        },
        text : false
      });
    } else {
      button.button();
    }
  });
}

function tableDraw(table, heightPercent) {
  if (heightPercent > 0) {
    var tableDiv = table.closest('div.table');

    var bodyDiv = tableDiv.closest('div.body');
    var bodyContent = tableDiv.closest('div.bodyContent');
    var tableScroll = $('div.dataTables_scrollBody', tableDiv);
    var scrollHeight = tableScroll.height();
    if (scrollHeight != null) {
      var collapsibleBox = tableDiv.closest('div.collapsibleBox');
      var otherHeight = $(bodyDiv).outerHeight(false) - bodyContent.height();
      var settings = table.fnSettings();
      var tableOverhead = collapsibleBox.outerHeight(false) - scrollHeight;
      var newHeight = Math.round(($(window).height() - otherHeight) * heightPercent)
          - tableOverhead - 15;
      if (newHeight < 50) {
        newHeight = 50;
      }
      if (newHeight != settings.oScroll.sY) {
        settings.oScroll.sY = newHeight;
      }
    }
  }
  table.fnDraw();
}

function tableShowEvents(table, heightPercent) {
  table.closest('.ui-accordion').bind('accordionchange', function(event, ui) {
    if ($(ui.panel).find(table).length > 0) {
      tableDraw(table, heightPercent);
    }
  });
  var tabs = table.closest('.ui-tabs');
  if (tabs.length > 0) {
    tabs.bind('tabsshow', function(event, ui) {
      if ($(ui.panel).find(table).length > 0) {
        tableDraw(table, heightPercent);
      }
    });

    var tab = $('.ui-tabs-panel:not(.ui-tabs-hide)', tabs);
    if (tab.find(table).length > 0) {
      tableDraw(table, heightPercent);
    }
    $(window).resize(function () {
      var tab = $('.ui-tabs-panel:not(.ui-tabs-hide)', tabs);
      if (tab.find(table).length > 0) {
        tableDraw(table, heightPercent);
      }
    });
  }
}

$(document).ready(
  function() {
    addConfirmButton({
      selector : 'button.delete',
      icon : 'trash',
      title : 'Confirm Delete',
      message : 'Are you sure you want to delete this record?'
    });
    refreshButtons($(document));
    $('div.collapsibleBox').each(function() {
      var active;
      if ($(this).hasClass('closed')) {
        active = false;
      } else {
        active = 0;
      }
      $(this).accordion({
        icons : {
          header : "ui-icon-triangle-1-e",
          headerSelected : "ui-icon-triangle-1-s"
        },
        collapsible : true,
        active : active,
        autoHeight : false,
        change : function(event, ui) {
          $('iframe.autoHeight', ui.newContent).iframeAutoHeight();
        }
      });
    });
    $('div.jqueryTabs').tabs({
      show : function(event, ui) {
        $('> iframe.autoHeight', ui.panel).iframeAutoHeight();
      },
      select : function(event, ui) {
        window.location.replace(ui.tab.hash);
      }
    });
    $('div.objectList table').dataTable({
      "bJQueryUI" : true,
      "bPaginate" : false,
      "bSort" : false
    });

    $('div.form').each(
      function() {
        var formWrapper = this;
        var form = $('form', this);
        var validate = form.validate({
          errorContainer : $('div.errorContainer', formWrapper),
          wrapper : "div class=\"errorMessage\"",
          highlight : function(element, errorClass, validClass) {
            $(element).closest('div.fieldComponent').addClass('invalid');
            $(element).addClass(errorClass).removeClass(validClass);
            $(element.form).find("label[for=" + element.id + "]").addClass(
              errorClass);
          },
          unhighlight : function(element, errorClass, validClass) {
            $(element).closest('div.fieldComponent').removeClass('invalid');
            $(element).removeClass(errorClass).addClass(validClass);
            $(element.form).find("label[for=" + element.id + "]").removeClass(
              errorClass);
          }
        });
        if ($(formWrapper).hasClass('formInvalid')) {
          validate.form();
        }
      });
  });
