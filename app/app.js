function initialize() {
    var filepathInput = $("#filepath");
    var scoreButton = $("#score");

    scoreButton.click(function() {
        var filepath = filepathInput.val();

        if(filepath == "") {
            alert("Please enter a file path");
            return
        }

        fetchScores(filepath);
    });

    $("a.examples").click(function(e) {
        e.preventDefault();
        var text = $(this).text();
        filepathInput.val(text);
    });

    var path = window.location.hash;
    if(path) {
        path = path.substr(1);
        fetchScores(path);
    }
}

function fetchScores(path) {
    path = path.trim("/")
    var url = "/nlp/" + path + "/?detailed=true&top=10";
    $.ajax({
       url: url,
       dataType: 'JSON',
       method: 'GET',
    }).then(function(result) {
        window.result = result;
        populateInfo(result);
        populateText(result);
    });
}

function populateInfo(result) {
    // populate the info boxes
    var scores = result.score;
    scores.forEach(function(score, i) {
        var element = $("td[data-n=" + (5-i) + "]");
        element.text(score);
    });

    $("#time").text(result.time + " ms");
}

function populateText(result) {
    if(result.detail) {
        var text = result.detail.text;

        text = replace_all_ngrams(result.detail.scores, ngram_tostring, text);

        $("#text").html(text);
    }
}

function ngram_tostring(ngram, score, token, index) {
    var ngramClass = "ngram " + token;
    if(index == 0) {
        ngramClass += " worst";
    }
    var scoreClass = "score " + token;
    score = Math.round(score * 100) / 100;
    return "<div class='" + ngramClass + "'>" + 
                ngram.join(" ") + 
                "<span class='" + scoreClass + "'>" + score + "</span>" +
            "</div>";
}

// returns the first occurrence of the ngram in the text
function ngram_pattern(ngram) {
    return new RegExp(ngram.join("\\W+"), "g");
}

// replacement = replacer(ngram, score)
function replace_all_ngrams(scores, replacer, text) {
    // generate magic tokens
    var prefix = "MAGIC_TOKEN_NLP______";
    var tokenMap = {};

    // replaces all ngrams with their tokens
    scores.forEach(function(entry, i) {
        var ngram = entry[0];
        var index = "" + i;
        for(var k = 0; k < 4 - index.length; k++) {
            index = "0" + index;
        }
        var token = prefix + index;
        tokenMap[token] = entry.concat([i]);

        text = text.replace(ngram_pattern(ngram), token)
    });

    // replace all tokens back to text
    for(var token in tokenMap) {
        var entry = tokenMap[token];
        var pattern = new RegExp(token, "g");
        text = text.replace(pattern, replacer(entry[0], entry[1], token, entry[2]));
    }

    return text;
}
