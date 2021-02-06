let A = getUApi()

A.makeH2({innerHTML: `All groups`})
let grpGrid = A.makeDiv({style: 'display: block; padding: 10px; border: solid 1px lightgray; border-radius: 5px;'});
let grpInfo = A.makeDiv({style: 'margin-top: 20px;'})

get(`/groups`).then(x => {
    let grps = JSON.parse(x.response)
    console.log(grps)
    grps.forEach(g => {
        let gTag = A.makeSpan({innerText: g, style: 'display: inline-block; padding: 2px 8px; background-color: lightgreen; margin: 2px; border-radius: 3px; cursor: pointer;'}, grpGrid)
        gTag.addEventListener('click', () => inodes.triggerSearch(`#inodesapp #viewgroup !${g}`))
    })
})
