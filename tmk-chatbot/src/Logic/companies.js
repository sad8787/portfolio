function getCompanies() {
    var $session = $jsapi.context().session;
    // Get_companies
    
    //var url = "https://hubhr.tmk-group.com/services/v1/staff.asmx/get_companies?";
    //var url = "https://hubhr.tmk-group.com/services/v1/staff.asmx/get_company_by_orgbe?";
    var token = "bf11df56-a75a-4f62-995f-1bd9d045a9ff";
    var orgbe ="1000"
    var leng = orgbe.length + token.length;
    //var url = "https://hubhr.tmk-group.com/services/v1/staff.asmx/get_employees_by_name?lastname=Осминников&firstname=Борис&middlename=Николаевич&orgGUID=131&token=bf11df56-a75a-4f62-995f-1bd9d045a9ff"
    
    
    //var url = "https://hubhr.tmk-group.com/services/v1/staff.asmx/get_company_by_orgbe?token=bf11df56-a75a-4f62-995f-1bd9d045a9ff&orgbe=1000"
    
    var headers = {
                //"token": token,
                //"orgbe":orgbe,
                "Content-Length": 200
                
            };
    
    var url = "https://hubhr.tmk-group.com/services/v1/staff.asmx/get_companies?token=bf11df56-a75a-4f62-995f-1bd9d045a9ff";
    var response = $http.get(url);
   
    if(response.status == 200){
        var text = "200";
        //employee
        //return text + ": "+ JSON.stringify(response.data.Employees.employee);
        ///get_company_by_orgbe?
        //return JSON.stringify(response.data.Companies.org);
        var result = response.data.Companies.org.length
        return result;
        //return JSON.stringify(result);
    }
    if(response.status == 411){
        var text = "411";
        return "error: 411 " + response.status +" : " + response.error;
    }
    if(response.status == -1){
        var text = "-1";
        return "error: -1 " + response.status +" : " + response.error;
    }
    return "error: " + response.status +" : " + response.error;
   
}