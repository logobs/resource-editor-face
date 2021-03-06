package com.lbs.re.ui.view.resourceitem.edit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.PrototypeScope;

import com.lbs.re.data.service.impl.language.LanguageServices;
import com.lbs.re.exception.localized.LocalizedException;
import com.lbs.re.model.ReResource;
import com.lbs.re.model.ReResourceitem;
import com.lbs.re.model.languages.ReAlbaniankv;
import com.lbs.re.model.languages.ReArabiceg;
import com.lbs.re.model.languages.ReArabicjo;
import com.lbs.re.model.languages.ReArabicsa;
import com.lbs.re.model.languages.ReAzerbaijaniaz;
import com.lbs.re.model.languages.ReBulgarianbg;
import com.lbs.re.model.languages.ReEnglishus;
import com.lbs.re.model.languages.ReFrenchfr;
import com.lbs.re.model.languages.ReGeorgiange;
import com.lbs.re.model.languages.ReGermande;
import com.lbs.re.model.languages.RePersianir;
import com.lbs.re.model.languages.ReRomanianro;
import com.lbs.re.model.languages.ReRussianru;
import com.lbs.re.model.languages.ReTurkishtr;
import com.lbs.re.model.languages.ReTurkmentm;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.spring.annotation.SpringComponent;

@SpringComponent
@PrototypeScope
public class ResourceItemTreeDataProvider {

	private static final long serialVersionUID = 1L;
	private BeanFactory beanFactory;

	private LanguageServices languageServices;

	@Autowired
	public ResourceItemTreeDataProvider(BeanFactory beanFactory, LanguageServices languageServices) {
		this.beanFactory = beanFactory;
		this.languageServices = languageServices;
	}

	/**
	 * Data provider.
	 */
	private TreeDataProvider<ReResourceitem> dataProvider;

	/**
	 * Gets data provider.
	 *
	 * @return Data provider.
	 */
	public TreeDataProvider<ReResourceitem> getTreeDataProvider() {
		return dataProvider;
	}

	public void buildTreeDataProvider(TreeData<ReResourceitem> items) {
		dataProvider = new TreeDataProvider<ReResourceitem>(items);
	}

	public void removeItem(ReResourceitem item) {
		dataProvider.getTreeData().removeItem(item);
	}

	public void addDataProviderListener(DataProviderListener listener) {
		dataProvider.addDataProviderListener(listener);
	}

	public void provideResourceItems(ReResource resource) throws LocalizedException {
		List<ReResourceitem> resourceItemList = resource.getReResourceitem();
		List<ReResourceitem> preparedData = loadTransientData(resourceItemList, resource.getId());

		orderResourceItemList(preparedData);
		buildTreeDataProvider(buildTreeData(preparedData));
	}

	public void refreshDataProviderByItems(List<ReResourceitem> resourceItemList) {
		getTreeDataProvider().getTreeData().clear();

		orderResourceItemList(resourceItemList);

		Stack<ReResourceitem> stack = new Stack<>();
		for (int i = 0; i < resourceItemList.size(); i++) {
			ReResourceitem reResourceitem = resourceItemList.get(i);
			if (reResourceitem.getLevelnr() == 0) {
				getTreeDataProvider().getTreeData().addItem(null, reResourceitem);
				stack.clear();
				stack.push(reResourceitem);
				continue;
			}
			while (reResourceitem.getLevelnr() <= stack.peek().getLevelnr()) {
				stack.pop();
			}
			getTreeDataProvider().getTreeData().addItem(stack.peek(), reResourceitem);
			stack.push(reResourceitem);
		}
		getTreeDataProvider().refreshAll();
	}

	public void orderResourceItemList(List<ReResourceitem> preparedData) {
		Collections.sort(preparedData, new Comparator<ReResourceitem>() {

			@Override
			public int compare(ReResourceitem o1, ReResourceitem o2) {
				int c;
				c = o1.getOrdernr().compareTo(o2.getOrdernr());
				if (c == 0) {
					c = o1.getLevelnr().compareTo(o2.getLevelnr());
				}
				if (c == 0) {
					c = o1.getTagnr().compareTo(o2.getTagnr());
				}
				return c;
			}
		});
	}

	public TreeData<ReResourceitem> buildTreeData(List<ReResourceitem> orderedList) {
		Stack<ReResourceitem> stack = new Stack<>();
		TreeData<ReResourceitem> treeData = new TreeData<ReResourceitem>();
		for (int i = 0; i < orderedList.size(); i++) {
			ReResourceitem reResourceitem = orderedList.get(i);
			if (reResourceitem.getLevelnr() == 0) {
				treeData.addItem(null, reResourceitem);
				stack.clear();
				stack.push(reResourceitem);
				continue;
			}
			while (reResourceitem.getLevelnr() <= stack.peek().getLevelnr()) {
				stack.pop();
			}
			treeData.addItem(stack.peek(), reResourceitem);
			stack.push(reResourceitem);
		}
		return treeData;
	}

	private List<ReResourceitem> loadTransientData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		loadTurkishData(resourceItemList, resourceId);
		loadAlbanianData(resourceItemList, resourceId);
		loadArabicEgData(resourceItemList, resourceId);
		loadArabicJoData(resourceItemList, resourceId);
		loadArabicSaData(resourceItemList, resourceId);
		loadAzerbaijaniAzData(resourceItemList, resourceId);
		loadBulgarianData(resourceItemList, resourceId);
		loadEnglishUsData(resourceItemList, resourceId);
		loadFrenchFrData(resourceItemList, resourceId);
		loadGeorgianData(resourceItemList, resourceId);
		loadGermanData(resourceItemList, resourceId);
		loadPersianData(resourceItemList, resourceId);
		loadRomanianData(resourceItemList, resourceId);
		loadRussianData(resourceItemList, resourceId);
		loadTurkmenData(resourceItemList, resourceId);
		return resourceItemList;
	}

	private void loadTurkishData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReTurkishtr> trList = languageServices.getTurkishService().getLanguageListByresourceref(resourceId);
		if (trList != null && !trList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReTurkishtr turkish : trList) {
					if (turkish.getResourceitemref().equals(item.getId())) {
						item.setTurkishTr(turkish.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadAlbanianData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReAlbaniankv> albenianList = languageServices.getAlbanianService().getLanguageListByresourceref(resourceId);
		if (albenianList != null && !albenianList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReAlbaniankv albanian : albenianList) {
					if (albanian.getResourceitemref().equals(item.getId())) {
						item.setAlbanianKv(albanian.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadArabicEgData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReArabiceg> arabicEgList = languageServices.getArabicEgService().getLanguageListByresourceref(resourceId);
		if (arabicEgList != null && !arabicEgList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReArabiceg arabicEg : arabicEgList) {
					if (arabicEg.getResourceitemref().equals(item.getId())) {
						item.setArabicEg(arabicEg.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadArabicJoData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReArabicjo> arabicJoList = languageServices.getArabicJoService().getLanguageListByresourceref(resourceId);
		if (arabicJoList != null && !arabicJoList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReArabicjo arabicJo : arabicJoList) {
					if (arabicJo.getResourceitemref().equals(item.getId())) {
						item.setArabicJo(arabicJo.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadArabicSaData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReArabicsa> arabicSaList = languageServices.getArabicSaService().getLanguageListByresourceref(resourceId);
		if (arabicSaList != null && !arabicSaList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReArabicsa arabicSa : arabicSaList) {
					if (arabicSa.getResourceitemref().equals(item.getId())) {
						item.setArabicSa(arabicSa.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadBulgarianData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReBulgarianbg> bulgarianBgList = languageServices.getBulgarianService().getLanguageListByresourceref(resourceId);
		if (bulgarianBgList != null && !bulgarianBgList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReBulgarianbg bulgarian : bulgarianBgList) {
					if (bulgarian.getResourceitemref().equals(item.getId())) {
						item.setBulgarianBg(bulgarian.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadAzerbaijaniAzData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReAzerbaijaniaz> azerbaijaniAzList = languageServices.getAzerbaijaniazService().getLanguageListByresourceref(resourceId);
		if (azerbaijaniAzList != null && !azerbaijaniAzList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReAzerbaijaniaz azerbaijaniAz : azerbaijaniAzList) {
					if (azerbaijaniAz.getResourceitemref().equals(item.getId())) {
						item.setAzerbaijaniAz(azerbaijaniAz.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadEnglishUsData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReEnglishus> englishUsList = languageServices.getEnglishService().getLanguageListByresourceref(resourceId);
		if (englishUsList != null && !englishUsList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReEnglishus englishUs : englishUsList) {
					if (englishUs.getResourceitemref().equals(item.getId())) {
						item.setEnglishUs(englishUs.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadFrenchFrData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReFrenchfr> frenchFrList = languageServices.getFrenchService().getLanguageListByresourceref(resourceId);
		if (frenchFrList != null && !frenchFrList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReFrenchfr frenchFr : frenchFrList) {
					if (frenchFr.getResourceitemref().equals(item.getId())) {
						item.setFrenchFr(frenchFr.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadGeorgianData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReGeorgiange> georgianList = languageServices.getGeorgianService().getLanguageListByresourceref(resourceId);
		if (georgianList != null && !georgianList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReGeorgiange georgian : georgianList) {
					if (georgian.getResourceitemref().equals(item.getId())) {
						item.setGeorgianGe(georgian.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadGermanData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReGermande> germanList = languageServices.getGermanService().getLanguageListByresourceref(resourceId);
		if (germanList != null && !germanList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReGermande german : germanList) {
					if (german.getResourceitemref().equals(item.getId())) {
						item.setGermanDe(german.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadPersianData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<RePersianir> persianList = languageServices.getPersianService().getLanguageListByresourceref(resourceId);
		if (persianList != null && !persianList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (RePersianir persian : persianList) {
					if (persian.getResourceitemref().equals(item.getId())) {
						item.setPersianIr(persian.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadRomanianData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReRomanianro> romanianList = languageServices.getRomanianService().getLanguageListByresourceref(resourceId);
		if (romanianList != null && !romanianList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReRomanianro romanian : romanianList) {
					if (romanian.getResourceitemref().equals(item.getId())) {
						item.setRomanianRo(romanian.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadRussianData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReRussianru> russianList = languageServices.getRussianruService().getLanguageListByresourceref(resourceId);
		if (russianList != null && !russianList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReRussianru russian : russianList) {
					if (russian.getResourceitemref().equals(item.getId())) {
						item.setRussianRu(russian.getResourcestr());
						break;
					}
				}

			}
		}
	}

	private void loadTurkmenData(List<ReResourceitem> resourceItemList, Integer resourceId) {
		List<ReTurkmentm> turkmenList = languageServices.getTurkmenService().getLanguageListByresourceref(resourceId);
		if (turkmenList != null && !turkmenList.isEmpty()) {
			for (ReResourceitem item : resourceItemList) {
				for (ReTurkmentm turkmen : turkmenList) {
					if (turkmen.getResourceitemref().equals(item.getId())) {
						item.setTurkmenTm(turkmen.getResourcestr());
						break;
					}
				}

			}
		}
	}
}
