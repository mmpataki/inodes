class automations {

    getCard(obj) {
        obj = JSON.parse(obj.content)
        return render('automation', {
            ele: 'div',
            classList: 'container',
            children: [
                { ele: 'h3', text: obj.name || "<no name, bug?>" },
                { ele: 'p', text: obj.description || "<no description, bug?>" }
            ]
        })
    }

    getEditor(obj) {

        class ISummaryStory {
            constructor(obj) { this.obj = obj }
            title() { return "Summary" }
            moral() { return this.obj }
            tell() { return render('summary', { ele: 'pre', text: JSON.stringify(this.obj, undefined, '  ') }) }
        }

        class ScriptIoEditor {
            constructor(obj) { this.obj = obj }
            title() { return "Provide inputs and script" }
            moral() { return { ...(this.obj || {}), script: this.script.value, inputs: this.getInputSpecs(), postInputTitleTemplate: this.postInputTitleTemplate.value } }
            isCompleted() { return true }
            nextStoryClass() { return ISummaryStory }
            getInputSpecs() {
                let ispecs = this.inputs.querySelectorAll('.automation-i-and-s-input-spec')
                let ret = []
                for (let i = 0; i < ispecs.length; i++) {
                    let spec = ispecs[i]
                    ret.push({ name: spec.querySelector('.automation-i-and-s-name').value, type: spec.querySelector('select').value, label: spec.querySelector('.automation-i-and-s-label').value })
                }
                return ret
            }
            tell() {
                let getInputBuilder = (inp) => {
                    let bldr = (x) => ({ ele: 'option', text: x, attribs: { value: x } })
                    let types = inodes.getKlassNames().concat('string')
                    return {
                        ele: 'div', classList: 'input-spec', children: [
                            { ele: 'input', classList: 'name', label: 'input name: ', attribs: { value: inp ? inp.name : "" } },
                            { ele: 'input', classList: 'label', label: 'label: ', attribs: { value: inp ? inp.label : "" } },
                            { ele: 'select', label: 'type: ', children: types.map(bldr), attribs: { value: inp ? inp.type : types[0] } },
                            { ele: 'button', text: '-', evnts: { click: function () { this.parentNode.remove() } } }
                        ]
                    }
                }

                let getInputs = () => {
                    if (!this.obj || !this.obj.inputs || !this.obj.inputs.length) return [getInputBuilder()]
                    return this.obj.inputs.map(input => getInputBuilder(input))
                }

                return render('automation-i-and-s', {
                    ele: 'div',
                    classList: 'container',
                    children: [
                        {
                            ele: 'div', iden: 'inputs', classList: 'inputs', label: 'Inputs',
                            children: [
                                ...getInputs(),
                                { ele: 'button', text: 'add input', evnts: { click: function () { this.parentNode.insertBefore(render('automation-i-and-s', getInputBuilder()), this) } } }
                            ]
                        },
                        {
                            ele: 'div', classList: 'inputs', label: 'Title template', children: [
                                { ele: 'input', iden: 'postInputTitleTemplate', attribs: { value: this.obj ? this.obj.postInputTitleTemplate || '' : '' } }
                            ]
                        },
                        {
                            ele: 'div', classList: 'inputs', label: 'Shell script', children: [
                                { ele: 'textarea', iden: 'script', attribs: { rows: 10, value: this.obj ? this.obj.script || '' : '' } }
                            ]
                        }
                    ]
                }, (id, ele) => this[id] = ele)
            }
        }

        class NameAndDescriptionStory {
            constructor(obj) { this.obj = obj }
            title() { return "Describe this automation unit" }
            type() { return this.autoType.querySelector("input[type='radio']:checked").value }
            moral() { return { ...(this.obj || {}), name: this.name.value, description: this.description.value, type: this.type() } }
            getErrMsg() { return "Fill in all the fields" }
            isCompleted() { return this.name.value && this.name.value.trim() != '' && this.description && this.description.value.trim() != '' }
            nextStoryClass() { return { 'shellscript': ScriptIoEditor }[this.type()] }
            tell() {
                let r = Math.random();
                return render('automation-name-and-desc', {
                    ele: 'div',
                    classList: 'container',
                    children: [
                        { ele: 'input', iden: 'name', label: 'Name', attribs: { value: this.obj ? this.obj.name : "" } },
                        { ele: 'textarea', iden: 'description', label: 'Description', attribs: { value: this.obj ? this.obj.description : "" } },
                        {
                            ele: 'div', iden: 'autoType', classList: 'types', label: 'Type of automation', children: [
                                { ele: 'input', attribs: { type: 'radio', name: `automation-type-${r}`, value: 'shellscript', checked: this.obj ? this.obj.type == 'shellscript' : false }, postlabel: 'shell script' }
                            ]
                        }
                    ]
                }, (id, ele) => this[id] = ele)
            }
        }

        if (obj) {
            obj = JSON.parse(obj.content);
        }
        let ele = render('automation-ed', { ele: 'div' }, (id, obj) => this[id] = obj);
        this.storyBoard = new StoryTeller(ele);
        this.storyBoard.openStory(NameAndDescriptionStory, obj);
        return ele;
    }

    getContent() {
        let story = this.storyBoard.currentStory();
        if (story && story.constructor.name == 'ISummaryStory') {
            return story.moral()
        }
        throw new Error('Please provide all inputs')
    }

}