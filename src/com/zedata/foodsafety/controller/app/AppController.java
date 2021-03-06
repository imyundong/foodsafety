package com.zedata.foodsafety.controller.app;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.zedata.foodsafety.controller.base.BaseController;
import com.zedata.foodsafety.entity.Page;
import com.zedata.foodsafety.entity.system.column.Column;
import com.zedata.foodsafety.entity.system.menu.Menu;
import com.zedata.foodsafety.entity.system.role.Role;
import com.zedata.foodsafety.entity.system.user.User;
import com.zedata.foodsafety.service.app.AppContentService;
import com.zedata.foodsafety.service.system.column.ColumnService;
import com.zedata.foodsafety.service.system.user.UserService;
import com.zedata.foodsafety.util.Const;
import com.zedata.foodsafety.util.PageData;
import com.zedata.foodsafety.util.RightsHelper;
import com.zedata.foodsafety.util.Tools;

/**
 * @类名:AppController.java 
 * @全路径包名： com.zedata.foodsafety.controller.app.AppController.java
 * @类功能描述: TODO
 * @公司名称：北龙泽达
 * @作者：bijiajin
 * @日期： 2017年1月16日 上午10:25:38 
 * @版本： V1.0   
 */
@Controller
@RequestMapping("/App")
public class AppController extends BaseController{
	@Resource(name="userService")
	private UserService userService;
	@Resource(name="columnService")
	private ColumnService columnService;
	@Resource(name="appContentService")
	private AppContentService appContentService;
	
	@RequestMapping(value="index")
	public ModelAndView toAppIndex(){
		ModelAndView mv = this.getModelAndView();
		mv.setViewName("newpage/newsline/timeline");
		PageData pd = new PageData();
		pd = this.getPageData();
		try {
			pd.put("SYSNAME", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
			//shiro管理的session
			Subject currentUser = SecurityUtils.getSubject();  
			Session session = currentUser.getSession();
			
			//从session中获取登录用户
			User user = (User)session.getAttribute(Const.SESSION_USER);
			if (user != null) {
				User userr = (User)session.getAttribute(Const.SESSION_USERROL);
				if(null == userr){
					user = userService.getUserAndRoleById(user.getUSER_ID());
					session.setAttribute(Const.SESSION_USERROL, user);
				}else{
					user = userr;
				}
				Role role = user.getRole();
				
				String columnRights = role!=null ? role.getRH_COLUMNS() : "";
				//避免每次拦截用户操作时查询数据库，以下将用户所属角色权限、用户权限限都存入session
				session.setAttribute(Const.SESSION_ROLE_RH_COLUMN, columnRights); 		//将角色栏目权限存入session
				
				
				List<Column> columnList = new ArrayList<Column>();
				if(null == session.getAttribute(Const.SESSION_allColumnList)){
					columnList = columnService.listAllColumn();
					if(Tools.notEmpty(columnRights)){
						for(Column colu : columnList){
							colu.setChecked(RightsHelper.testRights(columnRights, colu.getClon_id()));
							
						}
					}
					
					session.setAttribute(Const.SESSION_allColumnList, columnList);			//菜单权限放入session中
				}else{
					columnList = (List<Column>)session.getAttribute(Const.SESSION_allColumnList);
				}
				mv.addObject("columnList", columnList);
				mv.addObject("user", user);
			}else{
				mv.setViewName("system/admin/login");//session失效后跳转登录页面
			}
			
			
		} catch (Exception e) {
			mv.setViewName("system/admin/login");
			logger.error(e.getMessage(), e);
		}
		
		
		mv.addObject("pd",pd);
		return mv;
	}
	
	/**
	 * 进入tab标签
	 * @return
	 */
	@RequestMapping(value="/tab")
	public String tab(){
		return "newpage/newsline/tab";
//		return "system/admin/tab";
	}
	
	/**
	 * 进入首页后的默认页面
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/new_default")
	public ModelAndView defaultPage(Page page) throws Exception{
		ModelAndView mv = this.getModelAndView();
		mv.setViewName("newpage/newsline/new_list2");
		PageData pd = new PageData();
		pd = this.getPageData();
		String colunm = pd.getString("clon_name");
		if(!StringUtils.isEmpty(colunm)){
			switch (colunm) {
			case "今日关注":
				pd.put("jr", "jr");
				break;
			case "一周要闻":
				pd.put("yz", "yz");
				break;
			case "东莞信息":
				pd.put("is_dongguan", "is_dongguan");
				break;
			case "广东信息":
				pd.put("is_guangdong", "is_guangdong");
				break;
			default:
				break;
			}
		}
		page.setPd(pd);
		List<PageData> contentList = appContentService.listPdPageContent(page);
		mv.addObject("contentList", contentList);
		mv.addObject("pd", pd);
		return mv;
//		return "system/admin/default";
	}
	
	/**
	 * 进入首页后的默认页面
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/new_def")
	public ModelAndView defPage(Page page) throws Exception{
		String gotoHtml = "jinri_list";
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("jr", "jr");
		String keyboard = pd.getString("keywords");
		String newsType = pd.getString("n");
		String cloumName = pd.getString("cloumName");
		
			
		if(StringUtils.isEmpty(newsType))
			newsType = cloumName;
			
		
		if(!StringUtils.isEmpty(newsType)){
			switch (newsType) {
			case "jr":
				
				pd.put("n", "jr");
				gotoHtml = "jinri_list";
				break;
			case "yz":
				pd.put("yz", "yz");
				pd.put("n", "yz");
				pd.remove("jr");
				gotoHtml = "yizhou_list";
				break;
			case "dg":
				pd.put("is_dongguan", "is_dongguan");
				pd.put("n", "dg");
				pd.remove("jr");
				gotoHtml = "dongguan_list";
				break;
			case "gd":
				pd.put("is_guangdong", "is_guangdong");
				pd.put("n", "gd");
				pd.remove("jr");
				gotoHtml = "guangdong_list";
				break;
			case "gy":
				gotoHtml = "about";
				break;
			case "js":
				pd.put("js", "js");
				pd.put("n", "js");
				pd.remove("jr");
				gotoHtml = "js_list";
				break;
			default:
				break;
			}
		}
		
		if(!StringUtils.isEmpty(keyboard)){
			pd.put("keyboard", keyboard);
			pd.remove("jr");
		}
			
		
		mv.setViewName("newpage/newsline/"+gotoHtml);
		page.setPd(pd);
		
		try {
			pd.put("SYSNAME", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
			//shiro管理的session
			Subject currentUser = SecurityUtils.getSubject();  
			Session session = currentUser.getSession();
			
			//从session中获取登录用户
			User user = (User)session.getAttribute(Const.SESSION_USER);
			if (user != null) {
				User userr = (User)session.getAttribute(Const.SESSION_USERROL);
				if(null == userr){
					user = userService.getUserAndRoleById(user.getUSER_ID());
					session.setAttribute(Const.SESSION_USERROL, user);
				}else{
					user = userr;
				}
				Role role = user.getRole();
				
				String columnRights = role!=null ? role.getRH_COLUMNS() : "";
				//避免每次拦截用户操作时查询数据库，以下将用户所属角色权限、用户权限限都存入session
				session.setAttribute(Const.SESSION_ROLE_RH_COLUMN, columnRights); 		//将角色栏目权限存入session
				
				//获取栏目
				List<Column> columnList = new ArrayList<Column>();
				if(null == session.getAttribute(Const.SESSION_allColumnList)){
					columnList = columnService.listAllColumn();
					if(Tools.notEmpty(columnRights)){
						for(Column colu : columnList){
							colu.setChecked(RightsHelper.testRights(columnRights, colu.getClon_id()));
							
						}
					}
					
					session.setAttribute(Const.SESSION_allColumnList, columnList);			//菜单权限放入session中
				}else{
					columnList = (List<Column>)session.getAttribute(Const.SESSION_allColumnList);
				}
				mv.addObject("columnList", columnList);
				mv.addObject("user", user);
				//获取新闻列表
				List<PageData> contentList = appContentService.listPdPageContents(page);
				mv.addObject("contentList", contentList);
				
			}else{
				mv.setViewName("system/admin/login");//session失效后跳转登录页面
			}
			
			
		} catch (Exception e) {
			mv.setViewName("system/admin/login");
			logger.error(e.getMessage(), e);
		}
		
		//
		
		mv.addObject("pd", pd);
		mv.addObject("page", page);
		
		return mv;
//		return "system/admin/default";
	}
	
	@RequestMapping(value="/checkDetails")
	public ModelAndView checkDetails() throws Exception{
		ModelAndView mv = this.getModelAndView();
		mv.setViewName("newpage/newsline/new_details");
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = appContentService.findByID(pd);
		mv.addObject("msg", "edit");
		mv.addObject("pd", pd);
		return mv;
		
	}
	
	@RequestMapping(value="newList", produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String newList(Page page) throws Exception{
		PageData pd = new PageData();
		pd = this.getPageData();
		String nowpage = pd.getString("page");
		String newsType = pd.getString("cloumName");
		String keyboard = pd.getString("keywords");
		if(!StringUtils.isEmpty(newsType)){
			switch (newsType) {
			case "jr":
				pd.put("jr", "jr");
				pd.put("n", "jr");
				break;
			case "yz":
				pd.put("yz", "yz");
				pd.put("n", "yz");
				break;
			case "dg":
				pd.put("is_dongguan", "is_dongguan");
				pd.put("n", "dg");
				break;
			case "gd":
				pd.put("is_guangdong", "is_guangdong");
				pd.put("n", "gd");
				break;
			default:
				break;
			}
		}
		if(!StringUtils.isEmpty(keyboard))
			pd.put("keyboard", keyboard);
		if(!StringUtils.isEmpty(nowpage)){
			page.setCurrentResult((Integer.parseInt(nowpage)-1)*page.getShowCount());
		}
		pd.put("currentResult", page.getCResult());
		pd.put("showCount", page.getShowCount());
		page.setPd(pd);
		
		List<PageData> contentList = appContentService.findContentsList(page);
		
		String restHtml = createNewList(contentList,pd);
		return restHtml;
	}
	
	
	private String createNewList(List<PageData> pagelist,PageData pg){
		StringBuffer sb = new StringBuffer();
		for(PageData pagedata : pagelist){
			sb.append("<li class=\"item\"><span id=\"ad_extra\" style=\"display:none;\"></span>")
			.append("<div class=\"y-box item-inner\">")
			.append("<div class=\"y-left lbox\" ga_event=\"article_img_click\">")
			.append("<div class=\"y-left lbox\" ga_event=\"article_img_click\">")
			.append("<a class=\"img-wrap\" target=\"_blank\" href=\"").append(pagedata.get("link")).append("\">")
			.append("<img alt=\"\" src=\"").append(pg.get("url")).append("static/images/jr.jpg\">")
			.append("</a> </div> ")
			.append("<div class=\"rbox \">").append("<div class=\"rbox-inner\">").append("<div class=\"title-box\" ga_event=\"article_title_click\">")
			.append("<a class=\"link title\" target=\"_blank\" href=\" ").append(pagedata.get("link")).append("\">").append(pagedata.get("title")).append("</a>")
			.append("</div> <div class=\"y-box summary\">").append(pagedata.get("summary")).append("</div>")
			.append("<div class=\"y-box footer\">").append("<div class=\"y-left\">").append("<span class=\"lbtn\">&nbsp;").append(pagedata.get("source"))
			.append("</span> <span>&nbsp;<fmt:formatDate value=\"").append(pagedata.get("pubtime")).append("\" type=\"date\"/></span>")
			.append("</div> <div class=\"y-right\"> </div> </div> </div> </div> </div> </li>" )
			;
		}
		
		return sb.toString();
	}

}
