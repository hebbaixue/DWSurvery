package com.key.dwsurvey.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import com.key.common.QuType;
import com.key.common.utils.web.Struts2Utils;
import com.key.dwsurvey.dao.SurveyAnswerDao;
import com.key.dwsurvey.entity.AnChenFbk;
import com.key.dwsurvey.entity.SurveyDetail;
import com.key.dwsurvey.service.AnScoreManager;
import com.key.dwsurvey.service.SurveyDirectoryManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.key.common.base.entity.User;
import com.key.common.plugs.page.Page;
import com.key.common.plugs.page.PropertyFilter;
import com.key.common.service.BaseServiceImpl;
import com.key.common.utils.excel.XLSExportUtil;
import com.key.common.utils.parsehtml.HtmlUtil;
import com.key.dwsurvey.entity.AnAnswer;
import com.key.dwsurvey.entity.AnCheckbox;
import com.key.dwsurvey.entity.AnChenCheckbox;
import com.key.dwsurvey.entity.AnChenRadio;
import com.key.dwsurvey.entity.AnChenScore;
import com.key.dwsurvey.entity.AnCompChenRadio;
import com.key.dwsurvey.entity.AnDFillblank;
import com.key.dwsurvey.entity.AnEnumqu;
import com.key.dwsurvey.entity.AnFillblank;
import com.key.dwsurvey.entity.AnRadio;
import com.key.dwsurvey.entity.AnScore;
import com.key.dwsurvey.entity.AnYesno;
import com.key.dwsurvey.entity.QuCheckbox;
import com.key.dwsurvey.entity.QuChenColumn;
import com.key.dwsurvey.entity.QuChenOption;
import com.key.dwsurvey.entity.QuChenRow;
import com.key.dwsurvey.entity.QuMultiFillblank;
import com.key.dwsurvey.entity.QuRadio;
import com.key.dwsurvey.entity.QuScore;
import com.key.dwsurvey.entity.Question;
import com.key.dwsurvey.entity.SurveyAnswer;
import com.key.dwsurvey.entity.SurveyDirectory;
import com.key.dwsurvey.entity.SurveyStats;
import com.key.dwsurvey.service.AnAnswerManager;
import com.key.dwsurvey.service.AnCheckboxManager;
import com.key.dwsurvey.service.AnChenCheckboxManager;
import com.key.dwsurvey.service.AnChenFbkManager;
import com.key.dwsurvey.service.AnChenRadioManager;
import com.key.dwsurvey.service.AnChenScoreManager;
import com.key.dwsurvey.service.AnCompChenRadioManager;
import com.key.dwsurvey.service.AnDFillblankManager;
import com.key.dwsurvey.service.AnEnumquManager;
import com.key.dwsurvey.service.AnFillblankManager;
import com.key.dwsurvey.service.AnRadioManager;
import com.key.dwsurvey.service.AnYesnoManager;
import com.key.dwsurvey.service.QuestionManager;
import com.key.dwsurvey.service.SurveyAnswerManager;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;


/**
 * ??????????????????
 * @author keyuan(keyuan258@gmail.com)
 *
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
@Service
public class SurveyAnswerManagerImpl extends
		BaseServiceImpl<SurveyAnswer, String> implements SurveyAnswerManager {

	@Autowired
	private SurveyAnswerDao surveyAnswerDao;
	@Autowired
	private QuestionManager questionManager;
	@Autowired
	private AnYesnoManager anYesnoManager;
	@Autowired
	private AnRadioManager anRadioManager;
	@Autowired
	private AnFillblankManager anFillblankManager;
	@Autowired
	private AnEnumquManager anEnumquManager;
	@Autowired
	private AnDFillblankManager anDFillblankManager;
	@Autowired
	private AnCheckboxManager anCheckboxManager;
	@Autowired
	private AnAnswerManager anAnswerManager;
	@Autowired
	private AnChenRadioManager anChenRadioManager;
	@Autowired
	private AnChenCheckboxManager anChenCheckboxManager;
	@Autowired
	private AnChenFbkManager anChenFbkManager;
	@Autowired
	private AnCompChenRadioManager anCompChenRadioManager;
	@Autowired
	private AnChenScoreManager anChenScoreManager;
	@Autowired
	private AnScoreManager anScoreManager;
	@Autowired
	private SurveyDirectoryManager directoryManager;

	@Override
	public void setBaseDao() {
		this.baseDao = surveyAnswerDao;
	}

	@Override
	public void saveAnswer(SurveyAnswer surveyAnswer,
						   Map<String, Map<String, Object>> quMaps) {
		surveyAnswerDao.saveAnswer(surveyAnswer, quMaps);
	}

	@Override
	public List<Question> findAnswerDetail(SurveyAnswer answer) {
		String surveyId = answer.getSurveyId();
		String surveyAnswerId = answer.getId();
		List<Question> questions = questionManager.findDetails(surveyId, "2");
		for (Question question : questions) {
			getquestionAnswer(surveyAnswerId, question);
		}
		return questions;
	}

	/**
	 * ??????????????????
	 *
	 * @param surveyAnswerId
	 * @param question
	 * @return
	 */
	private int getquestionAnswer(String surveyAnswerId, Question question) {
		int score = 0;
		String quId = question.getId();
		// ????????????????????????,??????????????????????????????????????????
		QuType quType = question.getQuType();

		//????????????
		question.setAnAnswer(new AnAnswer());
		question.setAnCheckboxs(new ArrayList<AnCheckbox>());
		question.setAnDFillblanks(new ArrayList<AnDFillblank>());
		question.setAnEnumqus(new ArrayList<AnEnumqu>());
		question.setAnFillblank(new AnFillblank());
		question.setAnRadio(new AnRadio());
		question.setAnYesno(new AnYesno());
		question.setAnScores(new ArrayList<AnScore>());
		question.setAnChenRadios(new ArrayList<AnChenRadio>());
		question.setAnChenCheckboxs(new ArrayList<AnChenCheckbox>());
		question.setAnChenFbks(new ArrayList<AnChenFbk>());
		question.setAnCompChenRadios(new ArrayList<AnCompChenRadio>());
		question.setAnChenScores(new ArrayList<AnChenScore>());

		if (quType == QuType.YESNO) {// ???????????????
			AnYesno anYesno = anYesnoManager.findAnswer(surveyAnswerId, quId);
			if (anYesno != null) {
				question.setAnYesno(anYesno);
			}
		} else if (quType == QuType.RADIO || quType == QuType.COMPRADIO) {// ???????????????
			// ??????
			AnRadio anRadio = anRadioManager.findAnswer(surveyAnswerId, quId);
			if (anRadio != null) {
				question.setAnRadio(anRadio);
			}
		} else if (quType == QuType.CHECKBOX || quType == QuType.COMPCHECKBOX) {// ???????????????
			// ??????
			List<AnCheckbox> anCheckboxs = anCheckboxManager.findAnswer(
					surveyAnswerId, quId);
			if (anCheckboxs != null) {
				question.setAnCheckboxs(anCheckboxs);
			}
		} else if (quType == QuType.FILLBLANK) {// ?????????????????????
			AnFillblank anFillblank = anFillblankManager.findAnswer(
					surveyAnswerId, quId);
			if (anFillblank != null) {
				question.setAnFillblank(anFillblank);
			}

		} else if (quType == QuType.MULTIFILLBLANK) {// ?????????????????????
			List<AnDFillblank> anDFillblanks = anDFillblankManager.findAnswer(
					surveyAnswerId, quId);
			// System.out.println("anDFillblank.getAnswer():"+anDFillblank.getAnswer());
			if (anDFillblanks != null) {
				question.setAnDFillblanks(anDFillblanks);
			}
		} else if (quType == QuType.ANSWER) {// ???????????????
			AnAnswer anAnswer = anAnswerManager
					.findAnswer(surveyAnswerId, quId);
			if (anAnswer != null) {
				question.setAnAnswer(anAnswer);
			}
		} else if (quType == QuType.BIGQU) {// ????????????
			// List<Question> childQuestions=question.getQuestions();
			// for (Question childQuestion : childQuestions) {
			// score=getquestionAnswer(surveyAnswerId, childQuestion);
			// }
		} else if (quType == QuType.ENUMQU) {
			List<AnEnumqu> anEnumqus = anEnumquManager.findAnswer(
					surveyAnswerId, quId);
			if (anEnumqus != null) {
				question.setAnEnumqus(anEnumqus);
			}
		} else if (quType == QuType.SCORE) {// ?????????
			List<AnScore> anScores = anScoreManager.findAnswer(surveyAnswerId,
					quId);
			if (anScores != null) {
				question.setAnScores(anScores);
			}
		} else if (quType == QuType.CHENRADIO) {// ???????????????
			List<AnChenRadio> anChenRadios = anChenRadioManager.findAnswer(
					surveyAnswerId, quId);
			if (anChenRadios != null) {
				question.setAnChenRadios(anChenRadios);
			}
		} else if (quType == QuType.CHENCHECKBOX) {// ???????????????
			List<AnChenCheckbox> anChenCheckboxs = anChenCheckboxManager
					.findAnswer(surveyAnswerId, quId);
			if (anChenCheckboxs != null) {
				question.setAnChenCheckboxs(anChenCheckboxs);
			}
		} else if (quType == QuType.CHENFBK) {// ???????????????
			List<AnChenFbk> anChenFbks = anChenFbkManager.findAnswer(
					surveyAnswerId, quId);
			if (anChenFbks != null) {
				question.setAnChenFbks(anChenFbks);
			}
		} else if (quType == QuType.COMPCHENRADIO) {// ?????????????????????
			List<AnCompChenRadio> anCompChenRadios = anCompChenRadioManager
					.findAnswer(surveyAnswerId, quId);
			if (anCompChenRadios != null) {
				question.setAnCompChenRadios(anCompChenRadios);
			}
		}
		return score;
	}

	@Override
	public SurveyAnswer getTimeInByIp(SurveyDetail surveyDetail, String ip) {
		String surveyId = surveyDetail.getDirId();
		Criterion eqSurveyId = Restrictions.eq("surveyId", surveyId);
		Criterion eqIp = Restrictions.eq("ipAddr", ip);

		int minute = surveyDetail.getEffectiveTime();
		Date curdate = new Date();
		Calendar calendarDate = Calendar.getInstance();
		calendarDate.setTime(curdate);
		calendarDate.set(Calendar.MINUTE, calendarDate.get(Calendar.MINUTE)
				- minute);
		Date date = calendarDate.getTime();

		Criterion gtEndDate = Restrictions.gt("endAnDate", date);
		return surveyAnswerDao.findFirst("endAnDate", true, eqSurveyId, eqIp,
				gtEndDate);

	}

	@Override
	public Long getCountByIp(String surveyId, String ip) {
		String hql = "select count(*) from SurveyAnswer x where x.surveyId=? and x.ipAddr=?";
		Long count = (Long) surveyAnswerDao.findUniObjs(hql, surveyId, ip);
		return count;
	}

	@Override
	public List<SurveyAnswer> answersByIp(String surveyId, String ip) {
		Criterion criterionSurveyId = Restrictions.eq("surveyId", surveyId);
		Criterion criterionIp = Restrictions.eq("ipAddr", ip);
		List<SurveyAnswer> answers = surveyAnswerDao.find(criterionSurveyId,
				criterionIp);
		return answers;
	}


	@Override
	public String exportXLS(String surveyId, String savePath) {
		String basepath = surveyId + "";
		String urlPath = "/file/" + basepath + "/";// ?????????????????????
		String path = urlPath.replace("/", File.separator);// ??????????????????
		// File.separator +
		// "file" +
		// File.separator+basepath
		// + File.separator;
		savePath = savePath + path;
		File file = new File(savePath);
		if (!file.exists())
			file.mkdirs();

		SurveyDirectory surveyDirectory = directoryManager.getSurvey(surveyId);
		String fileName = surveyId + "_exportSurvey.xls";

		XLSExportUtil exportUtil = new XLSExportUtil(fileName, savePath);
		Criterion cri1 = Restrictions.eq("surveyId",surveyId);
		Page<SurveyAnswer> page = new Page<SurveyAnswer>();
		page.setPageSize(5000);
		try {
			page = findPage(page,cri1);
			int totalPage = page.getTotalPage();
			List<SurveyAnswer> answers = page.getResult();
			List<Question> questions = questionManager.findDetails(surveyId,"2");
			exportXLSTitle(exportUtil, questions);
			int answerListSize = answers.size();
			for (int j = 0; j < answerListSize; j++) {
				SurveyAnswer surveyAnswer = answers.get(j);
				String surveyAnswerId = surveyAnswer.getId();
				exportUtil.createRow(j+1);
				exportXLSRow(exportUtil, surveyAnswerId, questions, surveyAnswer);
				System.out.println(j+1+"/"+answerListSize);
			}
			exportUtil.exportXLS();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urlPath + fileName;
	}

	private void exportXLSRow(XLSExportUtil exportUtil,String surveyAnswerId, List<Question> questions,SurveyAnswer surveyAnswer) {
		int cellIndex = 0;
		for (Question question : questions) {
			getquestionAnswer(surveyAnswerId, question);
			QuType quType = question.getQuType();
			String quName = question.getQuName();
			String titleName = quType.getCnName();
			if (quType == QuType.YESNO) {// ?????????
				String yesnoAnswer = question.getAnYesno().getYesnoAnswer();
				if("1".equals(yesnoAnswer)){
					yesnoAnswer=question.getYesnoOption().getTrueValue();
				}else if("0".equals(yesnoAnswer)){
					yesnoAnswer=question.getYesnoOption().getFalseValue();
				}else{
					yesnoAnswer="";
				}
				exportUtil.setCell(cellIndex++, yesnoAnswer);
			} else if (quType == QuType.RADIO) {// ?????????
				String quItemId = question.getAnRadio().getQuItemId();
				List<QuRadio> quRadios=question.getQuRadios();
				String answerOptionName="";
				String answerOtherText="";
				boolean isNote = false;
				for (QuRadio quRadio : quRadios) {
					String quRadioId=quRadio.getId();
					if(quRadioId.equals(quItemId)){
						answerOptionName=quRadio.getOptionName();
						if(quRadio.getIsNote()==1){
							answerOtherText = question.getAnRadio().getOtherText();
							isNote = true;
						}
						break;
					}
				}
				answerOptionName=HtmlUtil.removeTagFromText(answerOptionName);
				answerOptionName = answerOptionName.replace("&nbsp;"," ");
				exportUtil.setCell(cellIndex++, answerOptionName);

//				answerOptionName=HtmlUtil.removeTagFromText(answerOptionName);
				if(isNote) exportUtil.setCell(cellIndex++, answerOtherText);
			} else if (quType == QuType.CHECKBOX) {// ?????????
				List<AnCheckbox> anCheckboxs=question.getAnCheckboxs();
				List<QuCheckbox> checkboxs = question.getQuCheckboxs();
				for (QuCheckbox quCheckbox : checkboxs) {
					String quCkId=quCheckbox.getId();
					String answerOptionName="0";
					String answerOtherText="";
					boolean isNote = false;
					for (AnCheckbox anCheckbox : anCheckboxs) {
						String anQuItemId=anCheckbox.getQuItemId();
						if(quCkId.equals(anQuItemId)){
							answerOptionName=quCheckbox.getOptionName();
							answerOptionName="1";
							if(quCheckbox.getIsNote() == 1){
								answerOtherText = anCheckbox.getOtherText();
								isNote = true;
							}
							break;
						}
					}
					answerOptionName=HtmlUtil.removeTagFromText(answerOptionName);
					answerOptionName = answerOptionName.replace("&nbsp;"," ");
					exportUtil.setCell(cellIndex++, answerOptionName);

					if(isNote) exportUtil.setCell(cellIndex++, answerOtherText);
				}
			} else if (quType == QuType.FILLBLANK) {// ?????????
				AnFillblank anFillblank=question.getAnFillblank();
				exportUtil.setCell(cellIndex++, anFillblank.getAnswer());
			} else if (quType == QuType.ANSWER) {// ???????????????
				AnAnswer anAnswer=question.getAnAnswer();
				exportUtil.setCell(cellIndex++, anAnswer.getAnswer());
			} else if (quType == QuType.COMPRADIO) {// ???????????????
				AnRadio anRadio=question.getAnRadio();
				String quItemId = anRadio.getQuItemId();
				List<QuRadio> quRadios=question.getQuRadios();
				String answerOptionName="";
				String answerOtherText="";
				for (QuRadio quRadio : quRadios) {
					String quRadioId=quRadio.getId();
					if(quRadioId.equals(quItemId)){
						answerOptionName=quRadio.getOptionName();
						answerOtherText=anRadio.getOtherText();
						break;
					}
				}
				answerOptionName=HtmlUtil.removeTagFromText(answerOptionName);
				answerOtherText=HtmlUtil.removeTagFromText(answerOtherText);
				answerOptionName = answerOptionName.replace("&nbsp;"," ");
				exportUtil.setCell(cellIndex++, answerOptionName);
				exportUtil.setCell(cellIndex++, answerOtherText);
			} else if (quType == QuType.COMPCHECKBOX) {// ???????????????
				List<AnCheckbox> anCheckboxs=question.getAnCheckboxs();
				List<QuCheckbox> checkboxs = question.getQuCheckboxs();
				for (QuCheckbox quCheckbox : checkboxs) {
					String quCkId=quCheckbox.getId();
					String answerOptionName="0";
					String answerOtherText="0";
					for (AnCheckbox anCheckbox : anCheckboxs) {
						String anQuItemId=anCheckbox.getQuItemId();
						if(quCkId.equals(anQuItemId)){
							answerOptionName=quCheckbox.getOptionName();
							answerOptionName="1";
							answerOtherText=anCheckbox.getOtherText();
							break;
						}
					}
					answerOptionName=HtmlUtil.removeTagFromText(answerOptionName);
					answerOptionName = answerOptionName.replace("&nbsp;"," ");
					exportUtil.setCell(cellIndex++, answerOptionName);
					if(1==quCheckbox.getIsNote()){
						answerOtherText=HtmlUtil.removeTagFromText(answerOtherText);
						exportUtil.setCell(cellIndex++, answerOtherText);
					}
				}
			} else if (quType == QuType.ENUMQU) {// ?????????
				List<AnEnumqu> anEnumqus=question.getAnEnumqus();
				int enumNum = question.getParamInt01();
				for (int i = 0; i < enumNum; i++) {
					String answerEnum="";
					for (AnEnumqu anEnumqu : anEnumqus) {
						if(i==anEnumqu.getEnumItem()){
							answerEnum=anEnumqu.getAnswer();
							break;
						}
					}
					exportUtil.setCell(cellIndex++,  answerEnum);
				}
			} else if (quType == QuType.MULTIFILLBLANK) {// ???????????????
				List<QuMultiFillblank> quMultiFillblanks = question.getQuMultiFillblanks();
				List<AnDFillblank> anDFillblanks=question.getAnDFillblanks();
				for (QuMultiFillblank quMultiFillblank : quMultiFillblanks) {
					String quMultiFillbankId=quMultiFillblank.getId();
					String answerOptionName="";
					for (AnDFillblank anDFillblank : anDFillblanks) {
						if(quMultiFillbankId.equals(anDFillblank.getQuItemId())){
							answerOptionName=anDFillblank.getAnswer();
							break;
						}
					}
					answerOptionName = answerOptionName.replace("&nbsp;"," ");
					exportUtil.setCell(cellIndex++, answerOptionName);
				}
			} else if (quType == QuType.SCORE) {// ?????????
				List<QuScore> quScores = question.getQuScores();
				List<AnScore> anScores=question.getAnScores();
				for (QuScore quScore : quScores) {
					String quScoreId=quScore.getId();
					String answerScore="";
					for (AnScore anScore : anScores) {
						if(quScoreId.equals(anScore.getQuRowId())){
							answerScore=anScore.getAnswserScore();
							break;
						}
					}
					exportUtil.setCell(cellIndex++, answerScore);
				}
			} else if (quType == QuType.CHENRADIO) {// ???????????????
				List<QuChenRow> quChenRows = question.getRows();
				List<QuChenColumn> quChenColumns = question.getColumns();
				List<AnChenRadio> anChenRadios=question.getAnChenRadios();
				for (QuChenRow quChenRow : quChenRows) {
					String quChenRowId=quChenRow.getId();
					String answerColOptionName="";
					boolean breakTag=false;
					for (QuChenColumn quChenColumn : quChenColumns) {
						String quChenColumnId=quChenColumn.getId();
						for (AnChenRadio anChenRadio : anChenRadios) {
							String anQuRowId=anChenRadio.getQuRowId();
							String anQuColId=anChenRadio.getQuColId();
							if(quChenRowId.equals(anQuRowId) && quChenColumnId.equals(anQuColId)){
								breakTag=true;
								break;
							}
						}
						if(breakTag){
							answerColOptionName=quChenColumn.getOptionName();
							break;
						}
					}
					answerColOptionName=HtmlUtil.removeTagFromText(answerColOptionName);
					answerColOptionName = answerColOptionName.replace("&nbsp;"," ");
					exportUtil.setCell(cellIndex++, answerColOptionName);
				}
			} else if (quType == QuType.CHENCHECKBOX) {
				List<QuChenRow> quChenRows = question.getRows();
				List<QuChenColumn> quChenColumns = question.getColumns();
				List<AnChenCheckbox> anChenCheckboxs = question.getAnChenCheckboxs();
				for (QuChenRow quChenRow : quChenRows) {
					String quChenRowId=quChenRow.getId();
					for (QuChenColumn quChenColumn : quChenColumns) {
						String quChenColumnId=quChenColumn.getId();
						String answerOptionName = "";
						for (AnChenCheckbox anChenCheckbox : anChenCheckboxs) {
							String anChenRowId=anChenCheckbox.getQuRowId();
							String anChenColumnId=anChenCheckbox.getQuColId();
							if(quChenRowId.equals(anChenRowId) && quChenColumnId.equals(anChenColumnId)){
								answerOptionName=quChenColumn.getOptionName();
								break;
							}
						}
						answerOptionName=HtmlUtil.removeTagFromText(answerOptionName);
						answerOptionName = answerOptionName.replace("&nbsp;"," ");
						exportUtil.setCell(cellIndex++, answerOptionName);
					}
				}
			} else if (quType == QuType.COMPCHENRADIO) {
				List<QuChenRow> quChenRows = question.getRows();
				List<QuChenColumn> quChenColumns = question.getColumns();
				List<QuChenOption> quChenOptions = question.getOptions();
				List<AnCompChenRadio> anCompChenRadios=question.getAnCompChenRadios();
				for (QuChenRow quChenRow : quChenRows) {
//					String optionName = quChenRow.getOptionName();
					String quChenRowId=quChenRow.getId();
					for (QuChenColumn quChenColumn : quChenColumns) {
						String answerOptionName="";
						String quChenColumnId=quChenColumn.getId();
						boolean breakTag=false;
						for (QuChenOption quChenOption : quChenOptions) {
							answerOptionName="";
							String quChenOptionId=quChenOption.getId();
							for (AnCompChenRadio anCompChenRadio : anCompChenRadios) {
								String anRowId=anCompChenRadio.getQuRowId();
								String anColumnId=anCompChenRadio.getQuColId();
								String anOptionId=anCompChenRadio.getQuOptionId();
								if(quChenRowId.equals(anRowId) && quChenColumnId.equals(anColumnId) && quChenOptionId.equals(anOptionId)){
									breakTag=true;
									break;
								}
							}
							if(breakTag){
								answerOptionName=quChenOption.getOptionName();
								break;
							}
						}
						answerOptionName=HtmlUtil.removeTagFromText(answerOptionName);
						answerOptionName = answerOptionName.replace("&nbsp;"," ");
						exportUtil.setCell(cellIndex++, answerOptionName);
					}
				}
			}
		}

		exportUtil.setCell(cellIndex++,  surveyAnswer.getIpAddr());
		exportUtil.setCell(cellIndex++,  surveyAnswer.getCity());
		exportUtil.setCell(cellIndex++,  new SimpleDateFormat("yyyy???MM???dd??? HH???mm???ss???").format(surveyAnswer.getEndAnDate()));


	}

	private void exportXLSTitle(XLSExportUtil exportUtil,
								List<Question> questions) {
		exportUtil.createRow(0);
		int cellIndex = 0;


		int quNum=0;
		for (Question question : questions) {
			quNum++;
			QuType quType = question.getQuType();

//			String quName = question.getQuName();
			String quName = question.getQuTitle();
			quName=HtmlUtil.removeTagFromText(quName);
			String titleName =quNum +"???" + quName + "[" + quType.getCnName() + "]";
			if (quType == QuType.YESNO) {// ?????????
				exportUtil.setCell(cellIndex++, titleName);
			} else if (quType == QuType.RADIO) {// ?????????
				List<QuRadio> quRadios=question.getQuRadios();
				boolean isNote = false;
				for (QuRadio quRadio : quRadios) {
					if(quRadio.getIsNote()==1){
						isNote = true;
					}
					break;
				}

				exportUtil.setCell(cellIndex++, titleName);
				if(isNote) exportUtil.setCell(cellIndex++, titleName + "????????????");

			} else if (quType == QuType.CHECKBOX) {// ?????????
				List<QuCheckbox> checkboxs = question.getQuCheckboxs();
				for (QuCheckbox quCheckbox : checkboxs) {
					String optionName = quCheckbox.getOptionName();
					optionName=HtmlUtil.removeTagFromText(optionName);
					exportUtil.setCell(cellIndex++,titleName + "???" + optionName );
					if(quCheckbox.getIsNote()==1){
						exportUtil.setCell(cellIndex++, titleName+ "???" + optionName  + "???????????????");
					}
				}
			} else if (quType == QuType.FILLBLANK) {// ?????????
				exportUtil.setCell(cellIndex++, titleName);
			} else if (quType == QuType.ANSWER) {// ???????????????
				exportUtil.setCell(cellIndex++, titleName);
			} else if (quType == QuType.COMPRADIO) {// ???????????????
				exportUtil.setCell(cellIndex++, titleName);
				exportUtil.setCell(cellIndex++, titleName+"-??????" );

			} else if (quType == QuType.COMPCHECKBOX) {// ???????????????
				List<QuCheckbox> checkboxs = question.getQuCheckboxs();
				for (QuCheckbox quCheckbox : checkboxs) {
					String optionName = quCheckbox.getOptionName();
					exportUtil.setCell(cellIndex++, titleName + "???"
							+ optionName);
					int isNote = quCheckbox.getIsNote();
					if (isNote == 1) {
						optionName=HtmlUtil.removeTagFromText(optionName);
						exportUtil.setCell(cellIndex++, titleName +"???"+ optionName
								+ "???" + "??????" );
					}
				}
			} else if (quType == QuType.ENUMQU) {// ?????????
				int enumNum = question.getParamInt01();
				for (int i = 0; i < enumNum; i++) {
					exportUtil.setCell(cellIndex++,  titleName + i + "?????????");
				}
			} else if (quType == QuType.MULTIFILLBLANK) {// ???????????????
				List<QuMultiFillblank> quMultiFillblanks = question
						.getQuMultiFillblanks();
				for (QuMultiFillblank quMultiFillblank : quMultiFillblanks) {
					String optionName = quMultiFillblank.getOptionName();

					optionName=HtmlUtil.removeTagFromText(optionName);
					exportUtil.setCell(cellIndex++, titleName + "???"
							+ optionName);
				}
			} else if (quType == QuType.SCORE) {// ?????????
				List<QuScore> quScores = question.getQuScores();
				for (QuScore quScore : quScores) {
					String optionName = quScore.getOptionName();
					optionName=HtmlUtil.removeTagFromText(optionName);
					exportUtil.setCell(cellIndex++, titleName+"???"+optionName);
				}
			} else if (quType == QuType.CHENRADIO) {// ???????????????
				List<QuChenRow> quChenRows = question.getRows();
				List<QuChenColumn> quChenColumns = question.getColumns();
				for (QuChenRow quChenRow : quChenRows) {
					String optionName = quChenRow.getOptionName();
					optionName=HtmlUtil.removeTagFromText(optionName);
					exportUtil.setCell(cellIndex++, titleName+ "-"
							+ optionName );
				}
			} else if (quType == QuType.CHENCHECKBOX) {// ???????????????
				List<QuChenRow> quChenRows = question.getRows();
				List<QuChenColumn> quChenColumns = question.getColumns();
				for (QuChenRow quChenRow : quChenRows) {
					String optionName = quChenRow.getOptionName();
					for (QuChenColumn quChenColumn : quChenColumns) {
						optionName=HtmlUtil.removeTagFromText(optionName);
						exportUtil.setCell(cellIndex++, titleName + "-"
								+ optionName + "-"
								+ quChenColumn.getOptionName() );
					}
				}
			} else if (quType == QuType.COMPCHENRADIO) {// ?????????????????????
				List<QuChenRow> quChenRows = question.getRows();
				List<QuChenColumn> quChenColumns = question.getColumns();
				for (QuChenRow quChenRow : quChenRows) {
					String optionName = quChenRow.getOptionName();
					for (QuChenColumn quChenColumn : quChenColumns) {
						optionName=HtmlUtil.removeTagFromText(optionName);
						exportUtil.setCell(cellIndex++,  titleName + "-"
								+ optionName + "-"
								+ quChenColumn.getOptionName());
					}
				}
			}
		}

		exportUtil.setCell(cellIndex++,  "?????????IP");
		exportUtil.setCell(cellIndex++,  "IP?????????");
		exportUtil.setCell(cellIndex++,  "????????????");

	}

	public void writeToXLS() {

	}

	@Override
	public SurveyStats surveyStatsData(SurveyStats surveyStats) {
		return surveyAnswerDao.surveyStatsData(surveyStats);
	}

	@Override
	public Page<SurveyAnswer> joinSurvey(Page<SurveyAnswer> page, User user) {
		if(user!=null){
			//??????????????????????????????ID???
			Criterion criterion=Restrictions.eq("userId", user.getId());
			page.setOrderBy("endAnDate");
			page.setOrderDir("desc");
			page=findPage(page, criterion);
			List<SurveyAnswer> answers=page.getResult();
			for (SurveyAnswer surveyAnswer : answers) {
				surveyAnswer.setSurveyDirectory(directoryManager.get(surveyAnswer.getSurveyId()));
			}
		}
		return page;
	}

	/**
	 * ??????????????????????????????
	 */
	public Page<SurveyAnswer> answerPage(Page<SurveyAnswer> page,String surveyId){
		Criterion cri1=Restrictions.eq("surveyId", surveyId);
		Criterion cri2=Restrictions.lt("handleState", 2);
		page.setOrderBy("endAnDate");
		page.setOrderDir("desc");
		page=findPage(page, cri1, cri2);
		return page;
	}


	@Override
	@Transactional
	public void delete(SurveyAnswer t) {
		if(t!=null){
			String belongAnswerId=t.getId();
			t.setHandleState(2);
			surveyAnswerDao.save(t);
			//????????????????????????????????????
			// ???????????????
			List<Question> questions = questionManager.findDetails(t.getSurveyId(),"2");
			for (Question question : questions) {
				String quId=question.getId();
				QuType quType = question.getQuType();

				if (quType == QuType.YESNO) {// ?????????

				} else if (quType == QuType.RADIO) {// ?????????
					AnRadio anRadio=anRadioManager.findAnswer(belongAnswerId, quId);
					if(anRadio!=null){
						anRadio.setVisibility(0);
						//????????????  1?????? 0?????????
						anRadioManager.save(anRadio);
					}
				} else if (quType == QuType.CHECKBOX) {// ?????????
					List<AnCheckbox> anCheckboxs=anCheckboxManager.findAnswer(belongAnswerId, quId);
					if(anCheckboxs!=null){
						for (AnCheckbox anCheckbox : anCheckboxs) {
							anCheckbox.setVisibility(0);
							//????????????  1?????? 0?????????
							anCheckboxManager.save(anCheckbox);
						}
					}
				} else if (quType == QuType.FILLBLANK) {// ?????????
					AnFillblank anFillblank=anFillblankManager.findAnswer(belongAnswerId, quId);
					if(anFillblank!=null){
						anFillblank.setVisibility(0);
						//????????????  1?????? 0?????????
						anFillblankManager.save(anFillblank);
					}
				} else if (quType == QuType.ANSWER) {// ???????????????

					AnAnswer anAnswer=anAnswerManager.findAnswer(belongAnswerId, quId);
					if(anAnswer!=null){
						anAnswer.setVisibility(0);
						//????????????  1?????? 0?????????
						anAnswerManager.save(anAnswer);
					}

				} else if (quType == QuType.COMPRADIO) {// ???????????????


				} else if (quType == QuType.COMPCHECKBOX) {// ???????????????

				} else if (quType == QuType.ENUMQU) {// ?????????

				} else if (quType == QuType.MULTIFILLBLANK) {// ???????????????
					List<AnDFillblank> anDFillblanks=anDFillblankManager.findAnswer(belongAnswerId, quId);
					if(anDFillblanks!=null){
						for (AnDFillblank anDFillblank : anDFillblanks) {
							anDFillblank.setVisibility(0);
							//????????????  1?????? 0?????????
							anDFillblankManager.save(anDFillblank);
						}
					}
				} else if (quType == QuType.SCORE) {// ?????????

					List<AnScore> anScores=anScoreManager.findAnswer(belongAnswerId, quId);
					if(anScores!=null){
						for (AnScore anScore : anScores) {
							anScore.setVisibility(0);
							//????????????  1?????? 0?????????
							anScoreManager.save(anScore);
						}

					}

				} else if (quType == QuType.CHENRADIO) {// ???????????????

					List<AnChenRadio> anChenRadios=anChenRadioManager.findAnswer(belongAnswerId, quId);
					if(anChenRadios!=null){
						for (AnChenRadio anChenRadio : anChenRadios) {
							anChenRadio.setVisibility(0);
							//????????????  1?????? 0?????????
							anChenRadioManager.save(anChenRadio);
						}
					}

				} else if (quType == QuType.CHENCHECKBOX) {// ???????????????

					List<AnChenCheckbox> anChenCheckboxs=anChenCheckboxManager.findAnswer(belongAnswerId, quId);
					if(anChenCheckboxs!=null){
						for (AnChenCheckbox anChenCheckbox : anChenCheckboxs) {
							anChenCheckbox.setVisibility(0);
							//????????????  1?????? 0?????????
							anChenCheckboxManager.save(anChenCheckbox);
						}

					}

				} else if (quType == QuType.COMPCHENRADIO) {// ?????????????????????

				} else if (quType == QuType.CHENSCORE) {// ????????????

					List<AnChenScore> anChenScores=anChenScoreManager.findAnswer(belongAnswerId, quId);
					if(anChenScores!=null){
						for (AnChenScore anChenScore : anChenScores) {
							anChenScore.setVisibility(0);
							//????????????  1?????? 0?????????
							anChenScoreManager.save(anChenScore);
						}
					}
				}

			}
		}
		super.delete(t);
	}


}
