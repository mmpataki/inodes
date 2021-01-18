<h3>Create new content-type (klass) in inodes</h3>

(function() {
    let pnode = document.currentScript.parentNode;
    let klass = document.createElement('textarea');
    klass.placeholder = "class json"
    klass.rows = "20"
    klass.style.width = "95%"
    pnode.appendChild(klass);
    let button = document.createElement('button');
    button.innerText = "Create"
    pnode.appendChild(button);
    button.addEventListener('click', function() {
        post(
            `/klass`,
            JSON.parse(klass.value),
            {'Content-Type': 'application/json'}
        )
        .then(a => alert('Success'))
        .catch(e => alert(`Failed : ${e}`)) 
    })
})();