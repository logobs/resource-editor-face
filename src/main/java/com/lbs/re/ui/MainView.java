/*
 * Copyright 2014-2019 Logo Business Solutions
 * (a.k.a. LOGO YAZILIM SAN. VE TIC. A.S)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.lbs.re.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.lbs.re.app.routing.PreferredDatabaseSession;
import com.lbs.re.app.security.SecurityUtils;
import com.lbs.re.app.security.UserSessionAttr;
import com.lbs.re.data.service.REUserService;
import com.lbs.re.data.service.ResourceService;
import com.lbs.re.exception.localized.LocalizedException;
import com.lbs.re.localization.LocaleConstants;
import com.lbs.re.localization.LocalizerManager;
import com.lbs.re.localization.ResourceEditorLocalizerWrapper;
import com.lbs.re.model.ReResource;
import com.lbs.re.ui.components.basic.REButton;
import com.lbs.re.ui.components.basic.RELabel;
import com.lbs.re.ui.components.basic.RETextField;
import com.lbs.re.ui.components.combobox.ResourceGroupComboBox;
import com.lbs.re.ui.components.layout.RECssLayout;
import com.lbs.re.ui.components.layout.REVerticalLayout;
import com.lbs.re.ui.navigation.NavigationManager;
import com.lbs.re.ui.util.RENotification;
import com.lbs.re.ui.util.RENotification.NotifyType;
import com.lbs.re.ui.view.message.MessageGridView;
import com.lbs.re.ui.view.resource.ResourceGridView;
import com.lbs.re.ui.view.resource.edit.ResourceEditView;
import com.lbs.re.ui.view.resourceitem.ResourceItemGridView;
import com.lbs.re.ui.view.user.UserGridView;
import com.lbs.re.ui.view.usersettings.UserSettingsView;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewLeaveAction;
import com.vaadin.spring.access.SecuredViewAccessControl;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The main view containing the menu and the content area where actual views are shown.
 * <p>
 * Created as a single View class because the logic is so simple that using a pattern like MVP would add much overhead for little gain. If more complexity is added to the class,
 * you should consider splitting out a presenter.
 */
@SpringViewDisplay
@UIScope
public class MainView extends HorizontalLayout implements ViewDisplay, ResourceEditorLocalizerWrapper {

	private static final long serialVersionUID = 1L;
	private final Map<Class<? extends View>, Button> navigationButtons = new HashMap<>();

	private final NavigationManager navigationManager;
	private final SecuredViewAccessControl viewAccessControl;
	private final REUserService userService;
	private final ResourceService resourceService;
	private PreferredDatabaseSession preferredDatabaseSession;

	private REVerticalLayout content;
	private RECssLayout menu;

	private REButton users;
	private REButton resources;
	private REButton messages;
	private REButton advancedSearch;
	private REButton userSettings;
	private REButton logout;
	private REButton goResource;
	private REButton language;
	private ResourceGroupComboBox resourceGroupComboBox;

	private UserSessionAttr userSession;

	@Autowired
	public MainView(NavigationManager navigationManager, SecuredViewAccessControl viewAccessControl, REUserService userService, PreferredDatabaseSession preferredDatabaseSession,
			ResourceGroupComboBox resourceGroupComboBox, ResourceService resourceService) throws LocalizedException {
		this.navigationManager = navigationManager;
		this.viewAccessControl = viewAccessControl;
		this.userService = userService;
		this.resourceService = resourceService;
		this.preferredDatabaseSession = preferredDatabaseSession;
		this.resourceGroupComboBox = resourceGroupComboBox;
		userSession = SecurityUtils.getCurrentUser(userService);
	}

	@PostConstruct
	public void init() throws LocalizedException {
		initComponents();
		attachNavigation(userSettings, UserSettingsView.class, SecurityUtils.getCurrentUser(userService).getReUser().getId());
		attachNavigation(users, UserGridView.class, "");
		attachNavigation(resources, ResourceGridView.class, "");
		attachNavigation(messages, MessageGridView.class, "");
		attachNavigation(advancedSearch, ResourceItemGridView.class, "");
	}

	private void initComponents() throws LocalizedException {
		setStyleName("app-shell");
		setSpacing(false);
		setSizeFull();
		setResponsive(true);

		content = new REVerticalLayout();
		content.setStyleName("content-container v-scrollable");
		content.setSizeFull();
		content.setMargin(false);

		addComponent(buildNavigationBar());
		addComponent(content);
		setExpandRatio(content, 1);

	}

	private RECssLayout buildNavigationBar() throws LocalizedException {
		RECssLayout navigationContainer = new RECssLayout();
		navigationContainer.setStyleName("navigation-bar-container");
		navigationContainer.setWidth("200px");
		navigationContainer.setHeight("100%");
		navigationContainer.addComponent(buildNavigation());
		return navigationContainer;
	}

	private RECssLayout buildNavigation() throws LocalizedException {
		RECssLayout navigation = new RECssLayout();
		navigation.setStyleName("navigation-bar");
		navigation.setSizeFull();

		REButton menuButton = new REButton("general.button.menu");
		menuButton.setIcon(VaadinIcons.ALIGN_JUSTIFY);
		menuButton.setStyleName("menu borderless");
		menuButton.setWidthUndefined();
		navigation.setWidthUndefined();
		navigation.addComponents(buildHeader(), buildProject(), menuButton, buildMenu());

		return navigation;
	}

	private Component buildHeader() {
		RELabel header = new RELabel();
		header.addStyleName("logo");
		header.setWidth("100%");
		header.setValue(getLocaleValue("view.mainview.header"));
		return header;
	}

	private Component buildProject() {
		RELabel projectLabel = new RELabel(preferredDatabaseSession.getPreferredDb().name());
		projectLabel.setStyleName("logo");
		projectLabel.setWidth("100%");
		return projectLabel;
	}

	private Component buildMenu() throws LocalizedException {
		menu = new RECssLayout();
		menu.setStyleName("navigation");

		RELabel userLabel = new RELabel(SecurityUtils.getCurrentUser(userService).getReUser().getUsername());
		userLabel.setStyleName("menuLabel");
		userLabel.setWidth("100%");

		RELabel mainLabel = new RELabel("Menu");
		mainLabel.setStyleName("menuLabel");
		mainLabel.setWidth("100%");

		resources = new REButton("view.mainview.resources", VaadinIcons.FOLDER);
		resources.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		messages = new REButton("view.mainview.messages", VaadinIcons.COMMENT_ELLIPSIS);
		messages.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		advancedSearch = new REButton("view.mainview.advancedsearch", VaadinIcons.SEARCH);
		advancedSearch.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		users = new REButton("view.mainview.usersview", VaadinIcons.USERS);
		users.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		userSettings = new REButton("view.mainview.usersettingsview", VaadinIcons.USER);
		userSettings.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		REVerticalLayout quickSearch = new REVerticalLayout();
		RETextField resourceNr = new RETextField("view.advancedsearch.resourcenumber", "half", false, true);
		resourceNr.setWidth("80%");
		goResource = new REButton("view.mainview.go", VaadinIcons.SIGN_OUT_ALT);
		goResource.addStyleName(ValoTheme.BUTTON_PRIMARY);
		goResource.setWidth("80%");
		quickSearch.setSpacing(true);
		quickSearch.addComponents(resourceGroupComboBox, resourceNr, goResource);

		logout = new REButton("view.mainview.logout", VaadinIcons.EXIT);
		logout.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		logout.addClickListener(e -> logout());

		language = new REButton("view.mainview.otherLanguage", VaadinIcons.GLOBE);
		language.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		language.addClickListener(e -> {
			try {
				changeLanguage(userSession.getLocale());
			} catch (LocalizedException e1) {
				e1.printStackTrace();
			}
		});

		goResource.addClickListener(e -> {
			int resourceNo = Integer.parseInt(resourceNr.getValue());
			ReResource resource = resourceService.getResourceByNumberAndGroup(resourceNo, resourceGroupComboBox.getValue());
			if (resource == null) {
				RENotification.showNotification(getLocaleValue("view.quicksearch.notfound"), NotifyType.WARNING);
			} else {
				navigationManager.navigateTo(ResourceEditView.class, resource.getId());
			}
		});

		menu.addComponents(quickSearch, mainLabel, advancedSearch, resources, messages, userLabel, userSettings, users, language, logout);
		return menu;
	}

	private void attachNavigation(Button navigationButton, Class<? extends View> targetView, Object parameter) {
		boolean hasAccessToView = viewAccessControl.isAccessGranted(targetView);
		navigationButton.setVisible(hasAccessToView);

		if (hasAccessToView) {
			navigationButtons.put(targetView, navigationButton);
			navigationButton.addClickListener(e -> navigationManager.navigateTo(targetView, parameter));
		}
	}

	@Override
	public void showView(View view) {
		content.removeAllComponents();
		content.addComponent(view.getViewComponent());
		navigationButtons.forEach((viewClass, button) -> button.setStyleName("selected", viewClass == view.getClass()));
	}

	/**
	 * Logs the user out after ensuring the currently open view has no unsaved changes.
	 */
	public void logout() {
		ViewLeaveAction doLogout = () -> {
			UI ui = getUI();
			ui.getSession().getSession().invalidate();
			ui.getPage().reload();
		};

		navigationManager.runAfterLeaveConfirmation(doLogout);
	}

	public void changeLanguage(Locale defaultLanguage) throws LocalizedException {
		UI ui = getUI();
		if (defaultLanguage.equals(LocaleConstants.LOCALE_TRTR)) {
			LocalizerManager.loadLocaleForAll(LocaleConstants.LOCALE_ENUS);
			userSession.setLocale(LocaleConstants.LOCALE_ENUS);
		} else if (defaultLanguage.equals(LocaleConstants.LOCALE_ENUS)) {
			LocalizerManager.loadLocaleForAll(LocaleConstants.LOCALE_TRTR);
			userSession.setLocale(LocaleConstants.LOCALE_TRTR);
		}
		ui.getPage().reload();
	}
}
