/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.0066275167785234896, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.03793103448275862, 500, 1500, "Delete list"], "isController": false}, {"data": [0.0, 500, 1500, "Edit item"], "isController": false}, {"data": [0.0, 500, 1500, "Add item"], "isController": false}, {"data": [0.004700854700854701, 500, 1500, "Delete item"], "isController": false}, {"data": [4.139072847682119E-4, 500, 1500, "Add list"], "isController": false}, {"data": [0.0, 500, 1500, "Get lists"], "isController": false}, {"data": [0.0, 500, 1500, "Edit list"], "isController": false}, {"data": [0.018890675241157555, 500, 1500, "Login"], "isController": false}, {"data": [0.0, 500, 1500, "Get items"], "isController": false}, {"data": [0.004664970313825276, 500, 1500, "Show item image"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 11920, 0, 0.0, 11479.380788590637, 13, 40384, 11035.0, 19021.0, 22226.399999999987, 29800.169999999976, 8.61836478096556, 2269.795579855937, 6.381562154759297], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["Delete list", 1160, 0, 0.0, 7765.628448275855, 13, 29593, 7232.5, 13721.0, 16491.9, 20722.150000000038, 0.9237375056539106, 0.44563117948538267, 0.6188319617954908], "isController": false}, {"data": ["Edit item", 1182, 0, 0.0, 12436.027918781727, 2968, 27125, 12912.5, 16895.9, 18604.899999999998, 22691.190000000002, 0.9281216137694929, 0.44502706285236426, 1.0947615750192377], "isController": false}, {"data": ["Add item", 1186, 0, 0.0, 13027.2925801012, 1644, 34073, 12556.0, 19452.5, 22643.399999999994, 31819.679999999997, 0.9066231650627489, 0.4356040988387426, 1.0678269605343724], "isController": false}, {"data": ["Delete item", 1170, 0, 0.0, 7685.3940170940205, 533, 31497, 6860.5, 13149.8, 15022.15, 20421.61, 0.9310557216955082, 0.4418877741640791, 0.6237345948077331], "isController": false}, {"data": ["Add list", 1208, 0, 0.0, 12415.633278145693, 891, 31943, 11180.5, 18521.000000000004, 21040.3, 29668.480000000003, 0.8858591073356175, 0.4247625211931525, 0.6635292337172056], "isController": false}, {"data": ["Get lists", 1222, 0, 0.0, 14931.078559738116, 4566, 40384, 13679.0, 22220.4, 24842.49999999999, 34443.49, 0.888528888459086, 952.4712411369788, 0.5171515796109523], "isController": false}, {"data": ["Edit list", 1196, 0, 0.0, 13810.264214046829, 2595, 34195, 12994.0, 20065.6, 22736.44999999999, 29885.0, 0.9023459485723166, 0.4397174104859239, 0.6741158697830296], "isController": false}, {"data": ["Login", 1244, 0, 0.0, 5306.981511254019, 262, 20457, 4757.5, 9510.5, 11394.0, 13827.749999999998, 0.8995133661612328, 0.6386193527336096, 0.42164689038807784], "isController": false}, {"data": ["Get items", 1173, 0, 0.0, 18501.203751065637, 6191, 40239, 17704.0, 25405.80000000002, 29618.6, 36434.72, 0.8808680417620064, 1198.8151456842065, 0.5144131728258592], "isController": false}, {"data": ["Show item image", 1179, 0, 0.0, 9007.369804919428, 452, 27020, 8737.0, 14148.0, 15554.0, 20851.80000000001, 0.9244086215413083, 179.32263417989117, 0.5461593906567299], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 11920, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
