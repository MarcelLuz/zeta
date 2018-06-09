import $ from "jquery";

export class codeoutline {

    constructor(element, metaModelId, dslType, editor) {
        this.metaModelId = metaModelId;
        this.dslType = dslType;
        this.editor = editor;
        this.createCodeOutline();
    }

    createCodeOutline() {
        fetch(`/rest/v2/meta-models/${this.metaModelId}/${this.dslType}`, {
            method: 'GET',
            credentials: 'same-origin'
        })
            .then(() => {
                switch (this.dslType) {
                    case "shape":
                        let nodes = this.findElementLineNumbers(this.editor, "node");
                        let edges = this.findElementLineNumbers(this.editor, "edge");
                        this.createOutlineLinks(nodes, this.editor);
                        this.createOutlineLinks(edges, this.editor);
                        break;
                    case "style":
                        let styles = this.findElementLineNumbers(this.editor, "style");
                        this.createOutlineLinks(styles, this.editor);
                        break;
                    default:
                        console.log(" ");
                }
            })
            .catch(err => {
                console.error(err);
                alert('an unexpected error occurred');
            });
    }

    createOutlineLinks(elements, editor) {
        let el = $("<div>").addClass("panel panel-default");
        let heading = this.createHeadline(elements);
        let body = $("<div>").addClass("panel-body");
        el.append(heading);
        el.append(body);
        let nodes = this.createLinks(elements, editor);
        for(let i = 0; i < nodes.length; i++)
            body.append(nodes[i]);
        $('#outline-nodes').append(el);
    }

    createHeadline(elements) {
        return $("<div>").text(this.capitalizeFirstLetter(elements[0].typ) + "s")
            .addClass("outline-heading")
            .addClass("panel-heading");
    }

    capitalizeFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }

    findElementLineNumbers(editor, typ) {
        let lines = editor.session.doc.getAllLines();
        let LineNumbers = [];
        for (let i = 0, l = lines.length; i < l; i++) {
            if (lines[i].indexOf(typ) == 0) {
                let obj = Object.assign({typ: typ, name: lines[i].split(" ")[1], line: (i + 1)});
                LineNumbers.push(obj);
            }
        }
        return LineNumbers
    }

    createLinks(elements, editor) {
        let links = [];
        for (let i = 0; i < elements.length; i++) {
            let obj = elements[i];
            let lineNumberEl = $("<span>").addClass("line").text(obj.line);
            let el = $("<div>")
                .attr("id", obj.line)
                .addClass(obj.name)
                .addClass(obj.typ)
                .addClass("outline-node")
                .text(obj.name)
                .bind("click", function () {
                    editor.scrollToLine(obj.line, true, true, function () {
                    });
                    editor.gotoLine(obj.line, 10, true);
                })
                .append(lineNumberEl);
            links.push(el);
        }
        return links;
    }

}