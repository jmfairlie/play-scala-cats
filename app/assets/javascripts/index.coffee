breeds = {"0":"Abyssinian", "1":"Aegean","2":"American Curl", "3":"Arabian Mau","4":"Asian", "5":"Bengal","6":"Chartreux", "7":"Khao Manee","8":"Turkish Angora", "9":"Ukrainian Levkoy"}
$ ->
  $.get "/cats", (clowder) ->
    #hide the list if empty
    if clowder.length == 0
        $("#clowderdiv").css("visibility", "hidden")
        
    $.each clowder, (index, cat) ->
      ic = $("<img>").attr('src','/assets/images/trash_empty.png')
      ac = $("<a>").attr('href', "javascript:pinga("+cat.id+",'"+ cat.name+ "');").append(ic)
      dele = $("<td>").addClass("delete").append(ac) 
      
      ie = $("<img>").attr('src','/assets/images/edit.png')
      ae = $("<a>").attr('href', '/cat/'+cat.id).append(ie)
      edit = $("<td>").addClass("edit").append(ae) 
      
      name = $("<td>").addClass("name").text cat.name
      color = $("<td>").addClass("color").css("background-color", cat.color).text cat.color
      breed = $("<td>").addClass("breed").text breeds[cat.breed]
      gender = $("<td>").addClass("gender").text if cat.gender == 0 then "Male" else "Female"
      
      if !!cat.filename
        image = $("<img>").attr('src',"/assets/uploads/"+cat.filename).attr('width','80').attr('height','80')
      else
        image = $("<img>").attr('src',"/assets/images/unknown.png").attr('width','80').attr('height','80')
      filename = $("<td>").addClass("filename").append(image)
      
      $("#catstable").append $("<tr>").append(dele).append(edit).append(name).append(color).append(breed).append(gender).append(filename)
      
  

#confirm dialogue
root = exports ? this      
root.pinga = (id, name) ->
    if confirm 'Are you sure you want to delete the kitten called ' + name + '?'
           window.location.href = "/delete/"+id
        
        