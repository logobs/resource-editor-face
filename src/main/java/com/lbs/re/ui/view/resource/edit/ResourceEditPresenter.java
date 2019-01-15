package com.lbs.re.ui.view.resource.edit;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vaadin.spring.events.EventBus.ViewEventBus;

import com.lbs.re.data.service.REUserService;
import com.lbs.re.data.service.ResourceService;
import com.lbs.re.data.service.ResourceitemService;
import com.lbs.re.exception.localized.LocalizedException;
import com.lbs.re.model.ReResource;
import com.lbs.re.model.ReResourceitem;
import com.lbs.re.ui.components.grid.REFilterGrid;
import com.lbs.re.ui.components.grid.RETreeGrid;
import com.lbs.re.ui.navigation.NavigationManager;
import com.lbs.re.ui.util.Enums.UIParameter;
import com.lbs.re.ui.util.Enums.ViewMode;
import com.lbs.re.ui.util.REStatic;
import com.lbs.re.ui.view.AbstractEditPresenter;
import com.lbs.re.ui.view.resource.ResourceGridView;
import com.lbs.re.ui.view.resourceitem.edit.ResourceItemDataProvider;
import com.lbs.re.ui.view.resourceitem.edit.ResourceItemTreeDataProvider;
import com.lbs.re.util.EnumsV2.ResourceGroupType;
import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;

@SpringComponent
@ViewScope
public class ResourceEditPresenter extends AbstractEditPresenter<ReResource, ResourceService, ResourceEditPresenter, ResourceEditView> {

	/**
	 * long serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private ResourceItemDataProvider resourceItemDataProvider;
	private ResourceItemTreeDataProvider resourceItemTreeDataProvider;
	private ResourceitemService resourceitemService;

	@SuppressWarnings("rawtypes")
	@Autowired
	public ResourceEditPresenter(ViewEventBus viewEventBus, NavigationManager navigationManager, ResourceService resourceService, REUserService userService,
			BeanFactory beanFactory, BCryptPasswordEncoder passwordEncoder, ResourceItemDataProvider resourceItemDataProvider,
			ResourceItemTreeDataProvider resourceItemTreeDataProvider, ResourceitemService resourceitemService) {
		super(viewEventBus, navigationManager, resourceService, ReResource.class, beanFactory, userService);
		this.resourceItemDataProvider = resourceItemDataProvider;
		this.resourceItemTreeDataProvider = resourceItemTreeDataProvider;
		this.resourceitemService = resourceitemService;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void enterView(Map<UIParameter, Object> parameters) throws LocalizedException {
		ReResource resource;
		if ((Integer) parameters.get(UIParameter.ID) == 0) {
			resource = new ReResource();
		} else {
			resource = getService().getById((Integer) parameters.get(UIParameter.ID));
			if (resource == null) {
				getView().showNotFound();
				return;
			}
		}
		refreshView(resource, (ViewMode) parameters.get(UIParameter.MODE));
		if (getItem().getId() != 0) {
			if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.LIST) {
				resourceItemDataProvider.provideResourceItems(resource);
				getView().getGridResourceItems().setVisible(true);
				getView().getTreeGridResourceItems().setVisible(false);
				getView().organizeResourceItemsGrid(resourceItemDataProvider);
			} else if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.TREE) {
				resourceItemTreeDataProvider.provideResourceItems(resource);
				getView().getGridResourceItems().setVisible(false);
				getView().getTreeGridResourceItems().setVisible(true);
				getView().organizeResourceItemsTreeGrid(resourceItemTreeDataProvider);
			}
			setVisibleButtons(true);
		} else {
			setVisibleButtons(false);
			getView().getGridResourceItems().setVisible(false);
			getView().getTreeGridResourceItems().setVisible(false);
		}
		getTitleForHeader();
	}

	@PostConstruct
	public void init() {
		subscribeToEventBus();
	}

	@SuppressWarnings("unchecked")
	public void removeResourceItemRow() throws LocalizedException {
		REFilterGrid<ReResourceitem> listGrid = getView().getGridResourceItems();
		RETreeGrid<ReResourceitem> treeGrid = getView().getTreeGridResourceItems();

		if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.TREE) {
			if (treeGrid.getSelectedItems().isEmpty()) {
				getView().showGridRowNotSelected();
				return;
			} else if (isActiveItemExists(treeGrid.getSelectedItems())) {
				getView().showActiveRowSelected();
				return;
			}
			treeGrid.getSelectedItems().forEach(resourceItem -> {
				try {
					resourceItemDataProvider.deleteLanguagesByItem((ReResourceitem) resourceItem);
					resourceitemService.delete((ReResourceitem) resourceItem);
				} catch (LocalizedException e) {
					e.printStackTrace();
				}
			});
			treeGrid.getSelectedItems().forEach(resourceItem -> treeGrid.getGridDataProvider().removeItem((ReResourceitem) resourceItem));
			treeGrid.deselectAll();
			treeGrid.refreshAll();
		} else if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.LIST) {
			if (listGrid.getSelectedItems().isEmpty()) {
				getView().showGridRowNotSelected();
				return;
			} else if (isActiveItemExists(treeGrid.getSelectedItems())) {
				getView().showActiveRowSelected();
				return;
			}
			listGrid.getSelectedItems().forEach(resourceItem -> {
				try {
					resourceItemDataProvider.deleteLanguagesByItem(resourceItem);
					resourceitemService.delete(resourceItem);
				} catch (LocalizedException e) {
					e.printStackTrace();
				}
			});

			listGrid.getSelectedItems().forEach(resourceItem -> listGrid.getGridDataProvider().removeItem(resourceItem));
			listGrid.deselectAll();
			listGrid.refreshAll();
		}
	}

	private boolean isActiveItemExists(Set<ReResourceitem> itemSet) {
		for (ReResourceitem item : itemSet) {
			if (item.getActive() == 1) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	protected void setActiveItems(boolean isActive) {
		REFilterGrid<ReResourceitem> listGrid = getView().getGridResourceItems();
		RETreeGrid<ReResourceitem> treeGrid = getView().getTreeGridResourceItems();

		if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.TREE) {
			if (treeGrid.getSelectedItems().isEmpty()) {
				getView().showGridRowNotSelected();
				return;
			}
			treeGrid.getSelectedItems().forEach(resourceItem -> {
				try {
					ReResourceitem item = (ReResourceitem) resourceItem;
					if (isActive) {
						item.setActive(1);
					} else {
						item.setActive(0);
					}
					resourceitemService.save(item);
				} catch (LocalizedException e) {
					e.printStackTrace();
				}
			});
			treeGrid.deselectAll();
			treeGrid.refreshAll();
		} else if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.LIST) {
			if (listGrid.getSelectedItems().isEmpty()) {
				getView().showGridRowNotSelected();
				return;
			}
			listGrid.getSelectedItems().forEach(resourceItem -> {
				try {
					ReResourceitem item = resourceItem;
					if (isActive) {
						item.setActive(1);
					} else {
						item.setActive(0);
					}
					resourceitemService.save(item);
				} catch (LocalizedException e) {
					e.printStackTrace();
				}
			});
			listGrid.getSelectedItems().forEach(resourceItem -> listGrid.getGridDataProvider().removeItem(resourceItem));
			listGrid.deselectAll();
			listGrid.refreshAll();
		}
	}

	public void prepareResourceItemWindow(ReResourceitem item, ViewMode mode) throws LocalizedException {
		Map<UIParameter, Object> windowParameters = REStatic.getUIParameterMap(item.getId(), ViewMode.VIEW);
		windowParameters.put(UIParameter.RESOURCE_ID, getItem().getId());
		getView().openResourceItemWindow(windowParameters);
	}

	@SuppressWarnings("unchecked")
	public void refreshGrid() {
		List<ReResourceitem> itemList = resourceitemService.getItemListByResource(getItem().getId());
		if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.TREE) {
			getView().getTreeGridResourceItems().getGridDataProvider().refreshDataProviderByItems(itemList);
		} else if (getItem().getResourcegroup().getResourceGroupType() == ResourceGroupType.LIST) {
			getView().getGridResourceItems().getGridDataProvider().refreshDataProviderByItems(itemList);
		}
		resourceItemDataProvider.loadTransientData(itemList, getItem().getId());
	}

	@Override
	protected Class<? extends View> getGridView() {
		return ResourceGridView.class;
	}

	@Override
	protected void getTitleForHeader() {
		String title = getView().getTitle();
		if (getItem().getResourcenr() != null) {
			title += ": " + getItem().getResourcenr();
		}
		getView().setTitle(title);
	}

	private void setVisibleButtons(boolean isVisible) {
		getView().getBtnAddRow().setVisible(isVisible);
		getView().getBtnRemoveRow().setVisible(isVisible);
		getView().getBtnActive().setVisible(isVisible);
		getView().getBtnDeActive().setVisible(isVisible);
	}
}
