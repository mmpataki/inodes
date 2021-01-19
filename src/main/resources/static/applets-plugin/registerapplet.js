<b>Register</b>

(function () {
    let pnode = document.currentScript.parentNode;

    let uid = document.createElement('input');
    let email = document.createElement('input');
    let password1 = document.createElement('input');
    let password2 = document.createElement('input');
    let register = document.createElement('button');
    let status = document.createElement('span');
    register.innerText = 'Register'

    let buts = [uid, email, password1, password2, register]
    let labl = ['user id', 'email', 'password', 'confirm password', 'register']

    for (let i = 0; i < buts.length; i++) {
        const e = buts[i];
        e.placeholder = labl[i]
        e.style.margin = "6px"
        e.style.display = 'block'
        pnode.appendChild(e)
    }
    pnode.appendChild(status)

    register.addEventListener('click', function () {
        if (password1.value != password2.value) {
            alert("Passwords don't match")
            return
        }
        post(
            `/auth/register`,
            {
                email: email.value,
                password: password1.value,
                user: uid.value
            },
            { 'Content-Type': 'application/json' }
        ).then(a => status.innerHTML = `<span style='color:green'>Registration successful. Open email and click the validation link to complete registration</span>`)
         .catch(e => alert(`Failed : ${e}`))
    })
})();