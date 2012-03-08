var prefs = new gadgets.Prefs();

var polls = [];
var currentPollIndex = 0;

function storePolls(response, params) {
  polls = response.data.polls;

  currentPollIndex = 0;
  if (polls.length > 0) {
    displayCurrentPoll();
  } else {
    displayNoPoll();
  }
}

function displayCurrentPoll() {
  displayPoll(polls[currentPollIndex]);
}

function displayPoll(poll, forceResultDisplay, hideNavigationBar) {
  if (typeof forceResultDisplay === 'undefined') {
    forceResultDisplay = 'false';
  }
  if (typeof hideNavigationBar === 'undefined') {
    hideNavigationBar = false;
  }

  emptyAndHideEverything();
  jQuery('#pollQuestion').html(poll.question);
  if ((forceResultDisplay === 'true') || poll.answered) {
    displayPollResultsHtml(poll);
  } else {
    displayPollQuestionHtml(poll);
  }

  if (hideNavigationBar != true) {
    // create the navigation bar
    createNavigationBar();
  }

  gadgets.window.adjustHeight();
}

function emptyAndHideEverything() {
  jQuery('.tools').empty();
  jQuery('.tools').hide();
  jQuery('#pollQuestion').empty();
  jQuery('#poll').empty();
  jQuery('#pollResult').empty();
  jQuery('#pollResult').hide();
  jQuery('#legend').empty();
  jQuery('#legend').hide();
  jQuery('#totalVotes').empty();
  jQuery('#totalVotes').hide();
}

function displayPollQuestionHtml(poll) {
  var pollHtml = '<div class="answers">';
  pollHtml += '<ul>'
  for (var i = 0; i < poll.answers.length; i++) {
    pollHtml += '<li>';
      pollHtml += '<label><input type="radio" name="answers" value="' + i + '"';
    if (i == 0) {
      pollHtml += ' checked="checked"';
    }
    pollHtml += '>' + poll.answers[i] + '</label>';
    pollHtml += '</li>';
  }
  pollHtml += '</ul>';
  pollHtml += '</div>';
  pollHtml += '<p><a class="answerButton" href="javascript:answerPoll()">' + prefs.getMsg('command.cast.vote') + '</a></p>';

  jQuery('#poll').html(pollHtml);
}

function displayPollResultsHtml(poll) {
  buildAndDisplayToolbar();
  buildAndDisplayChart('pie', poll);
  buildAndDisplayTotalVotes(poll);
}

function buildAndDisplayToolbar() {
  var toolbarHtml = '';
  toolbarHtml += '<select name="chartType" id="chartType">';
  toolbarHtml += '<option value="pie">' + prefs.getMsg('label.poll.chart.type.pie') + '</option>'
  toolbarHtml += '<option value="bars">' + prefs.getMsg('label.poll.chart.type.bars') + '</option>'
  toolbarHtml += '</select>';

  jQuery('.tools').html(toolbarHtml);
  jQuery('.tools').show();

  jQuery("#chartType").change(function () {
    var chartType = jQuery('#chartType option:selected').val();
    buildAndDisplayChart(chartType);
  });
}

function buildAndDisplayChart(chartType, poll) {
  chartType = chartType || 'pie';
  poll = poll || polls[currentPollIndex];

  if (chartType == 'pie') {
    var data = [];
    var resultsByAnswer = poll.result.resultsByAnswer;
    for( var i = 0; i < resultsByAnswer.length; i++) {
      var label = generateLabel(resultsByAnswer[i].answer, resultsByAnswer[i].result, poll.result.resultsCount);
      data[i] = { label: label, data: resultsByAnswer[i].result }
    }

    jQuery.plot(jQuery("#pollResult"), data, {
        legend: {
          container: jQuery('#legend')
        },
        series: {
          pie: {
            show: true
          }
        }
    });
  } else if (chartType == 'bars') {
    var data = [];
    var resultsByAnswer = poll.result.resultsByAnswer;
    for( var i = 0; i < resultsByAnswer.length; i++) {
      var label = generateLabel(resultsByAnswer[i].answer, resultsByAnswer[i].result, poll.result.resultsCount);
      data[i] = { label: label, data: [[i, resultsByAnswer[i].result]] };
    }

    jQuery.plot(jQuery("#pollResult"), data, {
      legend: {
        show: true,
        margin: 10,
        backgroundOpacity: 0.5,
        container: jQuery('#legend')
      },
      series: {
        bars: {
          show: true
        }
      },
      xaxis: {
        min: 0,
        tickSize: 1,
        tickFormatter: function (v) { return ""; }
      },
      yaxis: {
        min: 0,
        tickDecimals: 0
      }
    });
  }

  jQuery('#pollResult').show();
  jQuery('#legend').show();
}

function generateLabel(answer, result, resultsCount) {
  var label = '(0%)';
  if (!isNaN(result / resultsCount)) {
    label = answer + ' (' + (result / resultsCount) * 100 + '%)';
  }
  return label;
}

function buildAndDisplayTotalVotes(poll) {
  var totalVotes = prefs.getMsg('label.poll.total.votes') + ' ' + poll.result.resultsCount;
  jQuery('#totalVotes').html(totalVotes).show();
}

function createNavigationBar() {
  if (polls.length <= 0) {
    return;
  }

  var htmlContent = '';
  if (currentPollIndex > 0) {
    htmlContent += '<span class="previousButton"><a href="javascript:previousPoll()">' + prefs.getMsg('command.previous.poll') + '</a></span>';
  } else {
    htmlContent += '<span class="previousButton">' + prefs.getMsg('command.previous.poll') + '</span>';
  }
  htmlContent += '<span class="pollsInfo">' + polls.length + ' ' + prefs.getMsg('label.polls') + '</span>';
  if (currentPollIndex < polls.length - 1) {
    htmlContent += '<span class="nextButton"><a href="javascript:nextPoll()">' + prefs.getMsg('command.next.poll') + '</a></span>';
  } else {
    htmlContent += '<span class="nextButton">' + prefs.getMsg('command.next.poll') + '</span>';
  }

  jQuery('.navigationBar').html(htmlContent);
}

function previousPoll() {
  currentPollIndex -= 1;
  displayCurrentPoll();
}

function nextPoll() {
  currentPollIndex += 1;
  displayCurrentPoll();
}

function displayNoPoll() {
  emptyAndHideEverything();
  jQuery('#noPollLabel').html(prefs.getMsg('label.no.open.poll'));
  gadgets.window.adjustHeight();
}

function answerPoll() {
  var answerIndex = jQuery('input[name=answers]:checked').val();
  if (answerIndex >= 0) {
    var answerRequestParams = { operationId : 'Services.AnswerPoll',
      operationParams: {
        pollId: polls[currentPollIndex].pollId,
        answerIndex: answerIndex
      },
      operationContext: {},
      operationCallback: afterAnswerPoll
    };
    doAutomationRequest(answerRequestParams);
  }
}

function afterAnswerPoll(response, params) {
  var poll = response.data;
  // find the poll index
  for (var i = 0; i < polls.length; ++i) {
    if (polls[i].pollId == poll.pollId) {
      currentPollIndex = i;
      polls[i] = poll;
      break;
    }
  }
  displayPoll(poll);
}

gadgets.util.registerOnLoadHandler(function() {
  var pollId = prefs.getString("pollId");
  if (pollId != "") {
    loadOnePoll(pollId);
  } else {
    loadOpenPolls();
  }

});

function loadOpenPolls() {
  var NXRequestParams= { operationId : 'Services.GetOpenPolls',
    operationParams: {
      onlyUnansweredPolls: true
    },
    operationContext: {},
    operationCallback: storePolls
  };

  doAutomationRequest(NXRequestParams);
}

function loadOnePoll(pollId) {
  var NXRequestParams= { operationId : 'Services.GetPoll',
    operationParams: {
      pollId: pollId,
      withResult: true
    },
    operationContext: {},
    operationCallback: function(response, params) {
      var poll = response.data;
      currentPollIndex = 0;
      polls = [poll];
      displayPoll(poll, prefs.getString("displayResult"), true);
    }
  };

  doAutomationRequest(NXRequestParams);
}
