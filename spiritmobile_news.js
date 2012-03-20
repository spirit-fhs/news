/********************************************
 *  Classes Definition for spiritmobile News
 *  
 */



/**
 *  DataPair contains the Key & Value Data
 *  given by the JSON
 */ 
function datapair(k, v) {
    this.key = k;
    this.val = v;  
    
    /**
     * @description returns the key of the datapair
     * @return String value
     */
    this.getKey = function() {
        return this.key;
    };
    
    /**
     * @description returns the value of the datapair
     * @return String value
     */
    this.getValue = function() {
        return this.val;
    };
};            


/**
 *  SingleNews contains all the "Key & Value" Datapairs of a
 *  News given by the JSON
 */
function single_news(id) {
    this.datapairs = [];
    this.news_id;
    
    /**
     * @description adding a datapair to the datapair array
     */
    this.addDataPair = function(datapair) {
        this.datapairs.push(datapair);
    };
    
    /**
     * @description returns the array of all datapairs
     * @return array of datapair objects
     */
    this.getDataPairs = function() {
        return this.datapairs;
    };
    
    /**
     * @description returns the value the news_id
     * @return String value
     */
    this.getNewsID = function() {
        return this.news_id;
    };
};


/**
 *  NewsSet contains all Singlenews
 *  given by the JSON
 */
function news_set() {
    this.news = [];
    
    /**
     * @description adding a single news object to the news_array
     */
    this.addSingleNews = function(single_news) {
        this.news.push(single_news);
    };
    /**
     * @description returns all the news in the news_set
     * @return array of single_news objects
     */    
    this.getNews = function() {
        return this.news;
    };  
};            


/**
 * Object News contains a set of single News 
 *  - can loads all News From a JSON-Source via loadNews()-Function
 *  - can return a Set of News
 *  - can checkup whether the Set of News contains legal Data or not
 */
var News = function () {

    var tmp_news_set = new news_set();

    return {

        /**
         * @description loadNews() loads all News-Data from a JSON-Source
         */
        loadNews: function() {

            
            var bac = $.getJSON('news.json', function(data){                                       

                var i = 0;
                $.each(data, function() {

                    var tmp_singlenews = new single_news(i);
                    $.each(data[i], function(key, val) {
                        //old: show in div-container 
                        //$('#jsonloader').append('<p>' + key + ' ' + val + '</p>');

                        //new: store all Data in a NewsSet Object

                        var tmp_datapair = new datapair(key, val);
                        tmp_singlenews.addDataPair(tmp_datapair);

                    });
                    tmp_news_set.addSingleNews(tmp_singlenews);
                    i++;
                });

            });
            var fuck='sdfasdf';

        },

        /**
         * @description getNews() returns the Set of all News stored in the
         *              tmp_news_set Object
         */
        getNews: function() {
            return tmp_news_set;
        },

        /**
         * @description checkNews() is a deprecated function that checks the 
         *              ability to append all Key-Value pairs to a container
         *              named "jsonloader"
         */
        checkNews: function() {
            //$('#jsonloader').append('<p>' + tmp_news_set.getNews().length + '</p>');
            for (var j=0; j<tmp_news_set.getNews().length; j++) {
                for (var i=0; i<tmp_news_set.getNews()[j].getDataPairs().length; i++) {
                    $('#jsonloader').append('<p>' + tmp_news_set.getNews()[j].getDataPairs()[i].getKey() + ' '
                                                  + tmp_news_set.getNews()[j].getDataPairs()[i].getValue() + '</p>');
                }
                $('#jsonloader').append('<br/>');
            }
        },

        /**
         * @description showNewsOnElement(elem) appends all News-Data in the 
         *              right form to an HTML-Tag (especially a DIV-Container)
         */
        showNewsOnElement: function(elem) {
            $(elem).append('<ul data-role="listview" class="ui-listview" id="newslist"></ul>');
            $('#newslist').append('<li></li>');
            for (var j=0; j<tmp_news_set.getNews().length; j++) {
                $('#newslist').append('<li id="listelement-' + j + '"></li>');

                  //old: show semester + news_owner +  text (vertical)
//                            $('#listelement-' + j).append('<h1> ' + this.getValueFromKey("semester", j) + '</h1>' +
//                                                          '<p> '  + this.getValueFromKey("name", j) + '</p>'  
//                                                         );

                  //new: show semester + news_owner +  subject(horizontal)
                $('#listelement-' + j).append('<a href="#newsPage-' + j + '"><table border="0" cellspacing="5" width="100%" style="table-layout: fixed; overflow: hidden"><tr>' + 

                                                    '<td width="25%"><p>' + this.getValueFromKey("semester", j) + '</p></td>' +
                                                    '<td width="25%"><p>' + this.getValueFromKey("writer", j) + '</p></td>' +
                                                    '<td width="35%"><p>' + this.getValueFromKey("subject", j) + '</p></td>' +
                                                    '<td width="5%">' + ' ' + '</td>' +

                                                '</tr></table></a>');
                this.createNewsPage(j);
            }
            $('#newslist').append('<li></li>');

        },

        /**
         * @description getValueFromKey() returns the value that belongs to a
         *              specific key in a specific News
         */
        getValueFromKey: function(key, news_number) {
            for (var j=0; j<tmp_news_set.getNews().length; j++) {
                for (var i=0; i<tmp_news_set.getNews()[j].getDataPairs().length; i++) {
                    if ((tmp_news_set.getNews()[j].getDataPairs()[i].getKey() == key) && (j == news_number)) {
                        return tmp_news_set.getNews()[j].getDataPairs()[i].getValue();
                    }

                }
            }
        },

        /**
         * @description shortenString(str, size) resizes a given string to a 
         *              given size
         */
        shortenString: function(str, size) {
            if (str.length > size) {
                str = str.slice(0, size) + '...';
                return str;
            }
            else return str;
        },

        /**
         * @description createNewsPage(news_id) creates a detailed view of a 
         *              single News stored in the News-Set "tmp_news_set" 
         *              according to its number in the Set
         */
        createNewsPage: function(news_id) {

            // Seite erstellen (DIV-Container) --> ANHÄNGEN AN BODY TAG ÄNDERN (anhängen in container in newspage)!!
            $('body').append('<div data-role="page" id="newsPage-' + news_id + '"></div>');
            $('#newsPage-' + news_id).append(
                '<div data-role="header"> '                                       +
                    '<h1>' + this.getValueFromKey('subject', news_id) + '</h1>'   +
                    '<a class="button back" href="#p2">Back</a>'                + 
                '</div>'                                                        +
                '<div data-role="content">'                                            +
                    '<ul data-role="listview" data-inset="true">' + 
                        '<li><table width="100%"><tr>' + 
                                '<td>' + this.getValueFromKey('date', news_id) + 
                                '<td align="right">' + this.getValueFromKey('writer', news_id) + 
                        '</tr></table></li>' + 
                    '<li>' + this.getValueFromKey('news', news_id) + '</li>' + 
                    '</ul>' +
                '</div>'                                                        
                );   

        }

    };

};