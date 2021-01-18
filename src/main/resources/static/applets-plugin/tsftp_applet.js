<h3>Download TSFTP files</h3>
Customers share unzipped files to TSFTP and they unnecessarily hog the VPN b/w. So this webapp downloads, gzip's the files and serves the compressed files back. Paste the TSFTP path in the below textbox and click download <br/><br/>

(function () {
    let pnode = document.currentScript.parentNode;
    let path = document.createElement('input');
    path.placeholder = "TSFTP Path"
    path.style = "width: 55%; margin-right: 5px"
    pnode.appendChild(path);
    let button = document.createElement('button');
    button.innerText = "Download"
    pnode.appendChild(button);
    button.addEventListener('click', function() {
        let a = document.createElement('a');
        a.href = `http://10.65.168.176:5000/znd?path=${path.value}`
        a.target = "_blank"
        a.click()
    })
})();