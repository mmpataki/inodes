<h3>User</h3>

(function() {
    let capitalizeFirstLetter = function(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }
    let getInput = function() {
        return c('searchbox').value.split(/\s+/).filter(x => x[0] == '!')[0].substring(1)
    }
    let d = document.createElement('table')
    let user = getInput()
    get(`/auth/user/${user}`).then(function(resp) {
        let r = JSON.parse(resp.response)
        delete r.basic.password
        d.innerHTML = ""
        Object.keys(r.basic).forEach(function(k) {
            d.innerHTML += 
                `
                <tr>
                    <td>${capitalizeFirstLetter(k)}</td>
                    <td><b>${r.basic[k]}</b></td>
                </tr>
                `
        })
        d.innerHTML += `<tr> </tr><tr><td>Posts</td></tr>`
        Object.keys(r.postsCount).forEach(function(k) {
          d.innerHTML += `
            <tr>
            <td style="width: 30px; text-align: right"><b>${r.postsCount[k]}</b></td>
            <td style="padding-left: 10px">
               <a href="?q=${ encodeURIComponent("~" + user + " %" + k) }">
                  ${k}
               </a>
            </td>
            </tr>
          `
        });
    })
    let pnode = document.currentScript.parentNode;
    pnode.appendChild(d);
    pnode.appendChild('br');
})();