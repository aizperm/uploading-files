
async function test(){
    var input = document.querySelector('input[type="file"]')

    var data = new FormData()
    data.append('file', input.files[0])

    let response = await fetch('/file', {
      method: 'POST',
      body: data
    })
           
    let j = await response.json()
    console.log('json', j)
}