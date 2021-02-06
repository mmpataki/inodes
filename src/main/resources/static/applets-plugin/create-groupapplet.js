makeH2({innerHTML: 'Create Group'})

let gName = makeInput({label: 'Group Name'})
let gDesc = makeTextArea({label: 'Group Description'})

let but = makeButton({innerHTML: 'Create'})
but.addEventListener('click', function () {
    if(!gName.value) {
        return showError('group name is a required field')
    }
    post(
        `/groups/${gName.value}`,
        gDesc.value,
        {'Content-Type': 'application/json'},
    ).then(m => {
        showSuccess('Group created successfully.')
    })
})