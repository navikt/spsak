function loadJSON(path, container) {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                process(JSON.parse(xhr.responseText), container);
            }
            else {
                console.log("ERROR");
            }
        }
    };
    xhr.open('GET', path, true);
    xhr.send();
}

function nodeLabel(node) {
    var nodeLabel = "";
    var desc = node.ruleDescription;

    if (desc.size > 75) {
        nodeLabel = node.ruleId + "\n " + desc.substr(0, 75) + " ...";

    } else if (desc === "") {
        nodeLabel = node.ruleId;

    } else if (desc === node.ruleId) {
        nodeLabel = node.ruleId;
    } else {
        nodeLabel = node.ruleId + "\n " + desc;
    }
    return nodeLabel;
}

function process(json, container) {
    var root = json.nodes.find(function(node) {
        return node.id === json.root.id;
    });
    root.color = "lightgray";
    json.nodes.forEach(function(node) {
        node.shadow = "color:'rgb(0,255,0)'";
        node.label = nodeLabel(node);
        node.title = node.ruleId + ": " + node.ruleDescription;
    });
    var nodes = new vis.DataSet(json.nodes);

    json.edges.forEach(function(edge) {
        edge.arrows = "to";
        edge.from = edge.source;
        edge.to = edge.target;
        if (edge.role === "(IKKE )") {
            edge.role = "";
        }
        edge.label = edge.role;
    });
    var edges = new vis.DataSet(json.edges);

    var data = {
        nodes: nodes,
        edges: edges
    };
    var options = {
        height: '100%',
        width: '100%',
        edges: {
            font: {
                size: 8
            }
        },
        nodes: {
            shape: 'box',
            font: {
                size: 10
            },
            margin: 5,
            widthConstraint: {
                maximum: 170
            }
        },
        interaction: {
            hover: true
        },
        layout: {
            randomSeed: undefined,
            improvedLayout:true,
            hierarchical: {
                enabled:true,
                levelSeparation: 70,
                nodeSpacing: 300,
                treeSpacing: 200,
                blockShifting: true,
                edgeMinimization: true,
                parentCentralization: true,
                direction: 'UD',        // UD, DU, LR, RL
                sortMethod: 'directed'   // hubsize, directed
            }
        }
    };
    var network = new vis.Network(container, data, options);

}
