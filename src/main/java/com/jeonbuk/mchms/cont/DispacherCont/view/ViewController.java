package com.jeonbuk.mchms.cont.DispacherCont.view;

import com.jeonbuk.mchms.domain.City;
import com.jeonbuk.mchms.domain.Classification;
import com.jeonbuk.mchms.domain.DataDomain;
import com.jeonbuk.mchms.domain.FileEventDomain;
import com.jeonbuk.mchms.service.city.CityService;
import com.jeonbuk.mchms.service.classification.ClassificationService;
import com.jeonbuk.mchms.service.data.DataService;
import com.jeonbuk.mchms.service.fileevent.FileEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.*;

@Controller
public class ViewController {

    private static Logger logger = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    private DataService dataService;

    @Autowired
    private CityService cityService;

    @Autowired
    private ClassificationService classificationService;

    @Autowired
    private FileEventService fileEventService;

    @RequestMapping(value = "/MCHMSView", method = RequestMethod.GET)
    public ModelAndView mCHMSView(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();

        HttpSession session = request.getSession();
        mv.addObject("session", session);

        try {
            String id = request.getParameter("ID");
            DataDomain dataDomain = dataService.getDataInfo(id);

            if(dataDomain.getVisibility() == 1) {
                if(session != null || dataDomain.getRegistrant() != session.getAttribute("id")) {
                    response.setContentType("text/html; charset=UTF-8");

                    PrintWriter out = response.getWriter();

                    out.println("<script>alert('It is private data'); location.href='/MCHMSSearch/?City_id=" + dataDomain.getCityId() + "';</script>");
                    out.flush();
                    mv.setViewName("/MCHMSSearch/?City_id=" + dataDomain.getCityId());

                    return mv;
                }

            }

            String remarksEn = dataDomain.getRemarksEn();
            String referenceEn = dataDomain.getReferenceEn();

            if(StringUtils.isEmpty(remarksEn)) {
                remarksEn = "There is no content.";
            }

            if(StringUtils.isEmpty(referenceEn)) {
                referenceEn = "There is no content.";
            }

            double xPoint = 0;
            double yPoint = 0;

            if(dataDomain.getLatitude() != 0 && dataDomain.getLongitude() != 0) {
                xPoint = dataDomain.getLatitude();
                yPoint = dataDomain.getLongitude();
            }

            List<City> museums = cityService.getMuseums();

            FileEventDomain fileEventDomain = fileEventService.getFilesNameFromDataID(Integer.parseInt(id));

            String[] filesArray = {};
            String filesname =  "";
            if(fileEventDomain != null){
                filesname = fileEventDomain.getFiles();
                filesArray = filesname.split("\\|");
            }
            String ImgContents = "";
            for (int i=0; i<filesArray.length; i++){
                ImgContents = ImgContents + "<li class=\"bxslider\">\n" +
                        "<div id ="+ "\"viewdiv" + filesArray[i] + "\"" + "style=\"width:95%; margin:0 auto;\">\n" +
                        "<a href =\"/MCHMS/"+ filesArray[i] + "\">\n" +
                        "<img id =\"viewimg\" src=\"/MCHMS/" + filesArray[i] + "\"" + "style=\"cursor:pointer;\"/>\n" +
                        "</a>\n" +
                        "</div>\n" +
                        "</li>";
            }
            City cityInfo = cityService.getCityInfoById(id);
            Classification clInfo = classificationService.getClassificationInfoById(id);

            String cityClInfo = cityInfo.getCities() + "-" + cityInfo.getMuseum() + "-" + clInfo.getLarge();
            System.out.println("test : " + clInfo.getMiddle());
            System.out.println("test : " + clInfo.getSmall());
            System.out.println("test : " + clInfo.getSubSection());
            if (!(clInfo.getMiddle().equals(""))){
                cityClInfo = cityClInfo + "-" + clInfo.getMiddle();
                if (!(clInfo.getSmall().equals(""))){
                    cityClInfo = cityClInfo + "-" + clInfo.getSmall();
                    if (!(clInfo.getSubSection().equals(""))){
                        cityClInfo = cityClInfo + "-" + clInfo.getSubSection();
                    }
                }
            }

            mv.addObject("cityClInfo", cityClInfo);
            mv.addObject("ResultView", dataDomain);
            mv.addObject("Remarks_en", remarksEn);
            mv.addObject("Reference_en",referenceEn);
            mv.addObject("City_id", dataDomain.getCityId());
            mv.addObject("x", xPoint);
            mv.addObject("y", yPoint);
            mv.addObject("Museum", museums);
            mv.addObject("Clinfo", clInfo);
            mv.addObject("Cityinfo", cityInfo);
            mv.addObject("fileEventDomain", fileEventDomain);
            mv.addObject("filesname", filesname);
            mv.addObject("ImgContents", ImgContents);
            mv.addObject("file_length", filesArray.length);
            mv.setViewName("View/MCHMSView");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mv;
    }
}
