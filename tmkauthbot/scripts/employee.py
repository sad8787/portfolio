class Employee:    
    # конструктор класса
    def __init__(self, data_xml_as_string=None):
        if(data_xml_as_string == None):
            self.employeeGUID = None
            self.lastname = None
            self.firstname = None
            self.middlename = None  
            self.gender = None
            self.DOB = None
            self.insurance = None
            self.contract = None
            self.jobtitle = None
            self.id = None
            self.SamAccountName = None
            self.AdDomain = None
            self.UPN = None
            self.email = None
            self.orgGUID = None
            self.subdivisionGUID = None
            self.positionGUID = None
            self.category = None
            self.fired = None
            self.DateOfJobtitle = None
            self.DateOfFired = None
            self.DateOfAdmission = None
            self.Phones = None
            self.originalGUID = None
            self.PhotoDateTime = None
            self.EmployeeDateTime = None
            self.managerGUID = None
            self.mainorganization = None
            self.TypeContract = None
            self.BusinessDirect = None
            self.City = None
            self.vip = None
        else:
            import xml.etree.ElementTree as ET
            #reading from a file:
            #tree = ET.parse(addres)
            #root = tree.getroot() 
            #Or directly from a string:
            root = ET.fromstring(data_xml_as_string)      
            print(str(root.text))
            self.employeeGUID = root[0][0].text
            self.lastname = root[0][1].text
            self.firstname = root[0][2].text
            self.middlename = root[0][3].text  
            self.gender = root[0][4].text
            self.DOB = root[0][5].text
            self.insurance = root[0][6].text
            self.contract = root[0][7].text
            self.jobtitle = root[0][8].text
            self.id = root[0][9].text            
            self.SamAccountName = root[0][10].text
            self.AdDomain = root[0][11].text
            self.UPN = root[0][12].text
            self.email = root[0][13].text
            self.orgGUID = root[0][14].text
            self.subdivisionGUID = root[0][15].text
            self.positionGUID = root[0][16].text
            self.category = root[0][17].text
            self.fired = root[0][18].text
            self.DateOfJobtitle = root[0][19].text
            if(root[0][18].text== "false"):
                self.DateOfFired = None
            else:
                self.DateOfFired = root[0][20].text
            self.DateOfAdmission = root[0][21].text
            self.Phones = root[0][22].text
            self.originalGUID = root[0][23].text
            self.PhotoDateTime = root[0][24].text
            self.EmployeeDateTime = root[0][25].text
            self.managerGUID = root[0][26].text
            self.mainorganization = root[0][27].text
            self.TypeContract = root[0][28].text
            try:
                self.BusinessDirect = root[0][29].text
                self.City = root[0][30].text                       
                self.vip = root[0][31].text
            except (Exception) as error:
                self.BusinessDirect = None
                self.City = root[0][29].text                       
                self.vip = root[0][30].text
            
        
     