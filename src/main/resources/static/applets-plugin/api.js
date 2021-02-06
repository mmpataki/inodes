class UApi {

    constructor(pnode) {
        this.pnode = pnode;
    }

    /* interact with inodes app */
    getInput() {
        return c('searchbox').value.split(/\s+/).filter(x => x[0] == '!').map(x => x.substring(1))
    }

    /* element creation */
    makeInput(attribs, parent) {
        if (attribs && attribs.label) {
            parent = this.makeLabel({ innerHTML: attribs.label }, parent)
        }
        return this.makeEle('input', attribs, parent)
    }

    makeLabel(attribs, parent) {
        return this.makeEle('label', attribs, parent)
    }

    makeA(attribs, parent) {
        return this.makeEle('p', attribs, parent)
    }

    makeP(attribs, parent) {
        return this.makeEle('p', attribs, parent)
    }

    makeSpan(attribs, parent) {
        return this.makeEle('span', attribs, parent)
    }

    makeButton(attribs, parent) {
        return this.makeEle('button', attribs, parent)
    }

    makeDiv(attribs, parent) {
        return this.makeEle('div', attribs, parent)
    }

    makeCheckbox(attribs, parent) {
        if (attribs && attribs.label) {
            parent = this.makeLabel({ innerHTML: attribs.label }, parent)
        }
        return this.makeEle('checkbox', attribs, parent)
    }

    makeTextArea(attribs, parent) {
        if (attribs && attribs.label) {
            parent = this.makeLabel({ innerHTML: attribs.label }, parent)
        }
        return this.makeEle('textarea', attribs, parent)
    }

    makeH1(attribs, parent) { return this.makeEle('h1', attribs, parent) }
    makeH2(attribs, parent) { return this.makeEle('h2', attribs, parent) }
    makeH3(attribs, parent) { return this.makeEle('h3', attribs, parent) }
    makeH4(attribs, parent) { return this.makeEle('h4', attribs, parent) }
    makeH5(attribs, parent) { return this.makeEle('h5', attribs, parent) }
    makeH6(attribs, parent) { return this.makeEle('h6', attribs, parent) }

    makeEle(tag, attribs, parent) {
        let ele = document.createElement(tag);
        if (!parent) {
            parent = this.pnode;
        }
        parent.appendChild(ele);
        if (attribs) {
            Object.keys(attribs).forEach(k => {
                ele[k] = attribs[k];
            })
        }
        ele.classList += (` inodes-uapi-${tag}`)
        ele.clear = function () { this.innerHTML = '' }
        return ele
    }
}

function getUApi() {
    return new UApi(document.currentScript.parentNode)
}
