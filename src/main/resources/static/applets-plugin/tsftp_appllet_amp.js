<h4>Zip and Download TSFTP files</h4>

<style>
    #dolodolodolodolodolodolod {
        display: block;
        border: 3px solid black;
        border-radius: 20px;
        height: 50px;
        font-size: 30px;
        padding: 10px
    }
    
    .download {
        display: inline-block;
        background-color: rgb(27, 214, 27);
        margin: 20px;
        font-size: 30px;
        padding: 15px;
        font-weight: 900;
        text-decoration: none;
    }
</style>
<input type="text" id="dolodolodolodolodolodolod"></input>
<a class="download" id="dolodolodolodolodolodolodollodolodlock" target="_blank" onclick="download_file(event)">Download</a>
<span style="display:block">&copy;by <a target="_blank" href="https://github.com/achyutap">Achyuta Pataki</a></span>


function download_file(e) {
    e.target.href = `http://10.65.168.176:5000/znd?path=${document.getElementById('dolodolodolodolodolodolod').value}`
}