package com.revolsys.ui.html.builder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.context.HashMapContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.decorator.CollapsibleBox;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.decorator.FieldLabelDecorator;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.fields.LongField;
import com.revolsys.ui.html.fields.TextField;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.form.HtmlUiBuilderObjectForm;
import com.revolsys.ui.html.serializer.BuilderMethodLocaleSerializer;
import com.revolsys.ui.html.serializer.BuilderMethodSerializer;
import com.revolsys.ui.html.serializer.HtmlUiBuilderCollectionTableSerializer;
import com.revolsys.ui.html.serializer.HtmlUiBuilderDetailSerializer;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.serializer.type.BooleanSerializer;
import com.revolsys.ui.html.serializer.type.DateSerializer;
import com.revolsys.ui.html.serializer.type.DateTimeSerializer;
import com.revolsys.ui.html.serializer.type.TimestampSerializer;
import com.revolsys.ui.html.serializer.type.TypeSerializer;
import com.revolsys.ui.html.view.DetailView;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.ElementLabel;
import com.revolsys.ui.html.view.FilterableTableView;
import com.revolsys.ui.html.view.IFrame;
import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.html.view.RawContent;
import com.revolsys.ui.html.view.ResultPagerView;
import com.revolsys.ui.html.view.TableView;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.JexlUtil;

@ResponseStatus(reason = "Access Denied", value = HttpStatus.FORBIDDEN)
public class HtmlUiBuilder<T> implements BeanFactoryAware, ServletContextAware {

  private static final Pattern LINK_KEY_PATTERN = Pattern.compile("link\\(([\\w/]+),([\\w.]+)\\)");

  private static final Pattern SUB_KEY_PATTERN = Pattern.compile("^([\\w]+)(?:\\.(.+))?");

  public static HttpServletRequest getRequest() {
    final ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    final HttpServletRequest request = requestAttributes.getRequest();
    return request;
  }

  protected static String getUriTemplateVariable(final String name) {
    final Map<String, String> parameters = getUriTemplateVariables();
    return parameters.get(name);
  }

  public static Map<String, String> getUriTemplateVariables() {
    final HttpServletRequest request = getRequest();
    @SuppressWarnings("unchecked")
    final Map<String, String> uriTemplateVariables = (Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (uriTemplateVariables == null) {
      return Collections.emptyMap();
    } else {
      return uriTemplateVariables;
    }
  }

  private Map<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();

  private BeanFactory beanFactory;

  private HtmlUiBuilderFactory builderFactory;

  protected Map<Class<?>, TypeSerializer> classSerializers = new HashMap<Class<?>, TypeSerializer>();

  private int defaultPageSize = 25;

  private Map<String, String> fieldInstructions = new HashMap<String, String>();

  private final Map<String, Decorator> fieldLabels = new HashMap<String, Decorator>();

  private Map<String, Element> fields = new HashMap<String, Element>();

  protected String idParameterName;

  protected String idPropertyName = "id";

  /** The map of key lists for list views. */
  private Map<String, List<String>> keyLists = new HashMap<String, List<String>>();

  protected Map<String, KeySerializer> keySerializers = new HashMap<String, KeySerializer>();

  private Map<String, String> labels = new HashMap<String, String>();

  private final Logger log = Logger.getLogger(getClass());

  private int maxPageSize = 100;

  private Map<String, String> messages;

  protected Map<String, String> nullLabels = new HashMap<String, String>();

  private Map<String, String> pageUrls = new HashMap<String, String>();

  private String pluralTitle;

  protected String title;

  protected String typeName;

  private boolean usePathVariables = true;

  private Map<String, Page> pagesByName = new HashMap<String, Page>();

  public HtmlUiBuilder() {
    final Class<?> clazz = getClass();
    final Method[] methods = clazz.getMethods();
    for (int i = 0; i < methods.length; i++) {
      final Method method = methods[i];
      final String name = method.getName();

      final Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 2) {
        if (parameterTypes[0] == XmlWriter.class) {
          if (parameterTypes[1] == Object.class) {
            keySerializers.put(name, new BuilderMethodSerializer(this, method));
          }
        }
      } else if (parameterTypes.length == 3) {
        if (parameterTypes[0] == XmlWriter.class) {
          if (parameterTypes[1] == Object.class) {
            if (parameterTypes[2] == Locale.class) {
              keySerializers.put(name, new BuilderMethodLocaleSerializer(this,
                method));
            }
          }
        }
      }
    }
    classSerializers.put(Date.class, new DateTimeSerializer());
    classSerializers.put(java.sql.Date.class, new DateSerializer());
    classSerializers.put(Timestamp.class, new TimestampSerializer());
    classSerializers.put(Boolean.class, new BooleanSerializer());
  }

  public HtmlUiBuilder(final String typeName, final String title) {
    this(typeName, title, title);
  }

  public HtmlUiBuilder(final String typeName, final String title,
    final String pluralTitle) {
    this();
    setTypeName(typeName);
    this.title = title;
    this.pluralTitle = pluralTitle;
  }

  public void addCollapsibleIframe(
    final ElementContainer container,
    final Class<?> builderClass,
    final String pageName) {
    addCollapsibleIframe(container, builderClass.getName(), pageName);
  }

  public void addCollapsibleIframe(
    final ElementContainer container,
    final String builderName,
    final String pageName) {
    final HtmlUiBuilder<?> appBuilder = getBuilder(builderName);
    final Page page = appBuilder.getPage(pageName);
    if (page != null) {
      final Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("plain", true);
      final String link = page.getFullUrl(parameters);
      String title = page.getExpandedTitle();
      final Decorator decorator = new CollapsibleBox(title);
      final IFrame iFrame = new IFrame(link, "autoHeight", decorator);
      container.add(iFrame);
    }
  }

  public void addKeySerializer(
    final String key,
    final KeySerializer keySerializer) {
    keySerializers.put(key, keySerializer);
  }

  public void addMenuElement(final ElementContainer container, final Menu menu) {
    if (menu.getMenus().size() > 0) {
      final MenuElement actionMenuElement = new MenuElement(menu, "actionMenu");
      container.add(actionMenuElement);
    }
  }

  public Menu addMenuItem(
    final Menu menu,
    final String prefix,
    final String pageName,
    final String linkTitle) {
    final Map<String, Object> parameters = Collections.<String, Object> emptyMap();
    return addMenuItem(menu, prefix, pageName, linkTitle, parameters);
  }

  public Menu addMenuItem(
    final Menu menu,
    final String prefix,
    final String pageName,
    final String linkTitle,
    final Map<String, Object> parameters) {
    return addMenuItem(menu, prefix, pageName, linkTitle, null, parameters);
  }

  public Menu addMenuItem(
    final Menu menu,
    final String prefix,
    final String pageName,
    final String linkTitle,
    final String target,
    final Map<String, Object> parameters) {
    final Page page = getPage(prefix, pageName);
    if (page != null) {
      final String addUrl = page.getFullUrl(parameters);
      final Menu menuItem = new Menu(linkTitle, addUrl);
      menuItem.setTarget(target);
      menu.addMenuItem(menuItem);
      return menuItem;
    }
    return null;
  }

  public Menu addMenuItem(
    final Menu menu,
    final String prefix,
    final String pageName,
    final String linkTitle,
    final String target) {
    final Map<String, Object> parameters = Collections.<String, Object> emptyMap();
    return addMenuItem(menu, prefix, pageName, linkTitle, target, parameters);
  }

  public void addMessageView(
    final ElementContainer view,
    final String messageName,
    final Map<String, Object> variables) {
    final String message = getMessage(messageName, variables);
    if (message != null) {
      view.add(new RawContent(message));
    }
  }

  /**
   * @param key
   * @param label
   */
  public void addNullLabel(final String key, final String label) {
    nullLabels.put(key, label);
  }

  public void addOrderBy(final String name, final boolean ascending) {
    orderBy.put(name, ascending);
  }

  public Element createDetailView(
    final Object object,
    final String cssClass,
    final List<String> keys,
    final Locale locale) {
    final HtmlUiBuilderDetailSerializer model = new HtmlUiBuilderDetailSerializer(
      this, keys, locale);
    model.setObject(object);
    return new DetailView(model, cssClass);
  }

  public Element createDetailView(
    final Object object,
    final String cssClass,
    final String keyList,
    final Locale locale) {
    return createDetailView(object, cssClass, getKeyList(keyList), locale);
  }

  public FilterableTableView createFilterableTableView(
    final String cssClass,
    final String title,
    final List<String> keys,
    final Locale locale,
    final boolean showSearchFields) {

    final HtmlUiBuilderCollectionTableSerializer model = new HtmlUiBuilderCollectionTableSerializer(
      this, keys, locale);
    return new FilterableTableView(this, model, cssClass + " " + typeName,
      title, keys, showSearchFields);

  }

  public FilterableTableView createFilterableTableView(
    final String cssClass,
    final String title,
    final String keyList,
    final Locale locale,
    final boolean showSearchFields) {
    final List<String> keyListkeyList = getKeyList(keyList);
    return createFilterableTableView(cssClass, title, keyListkeyList, locale,
      showSearchFields);
  }

  /**
   * Create a form for the object using the specified list of fields keys. The
   * form is created without the form title.
   * 
   * @param <F> The type of form to return.
   * @param object The object to create the form for.
   * @param fieldKeys The list of keys for the fields to include on the form.
   * @param locale The locale.
   * @return The generated form.
   */
  @SuppressWarnings("unchecked")
  public <F extends Form> F createForm(
    final Object object,
    final List<String> fieldKeys,
    final Locale locale) {
    final HtmlUiBuilderObjectForm form = createForm(object, getTypeName(),
      fieldKeys, locale);
    return (F)form;
  }

  @SuppressWarnings("unchecked")
  public <F extends Form> F createForm(
    final Object object,
    final String formName,
    final List<String> keys,
    final Locale locale) {
    final HtmlUiBuilderObjectForm form = new HtmlUiBuilderObjectForm(object,
      this, formName, keys);
    return (F)form;
  }

  /**
   * Create a form for the object using the specified list of fields keys. The
   * form is created without the form title.
   * 
   * @param <T> The type of form to return.
   * @param object The object to create the form for.
   * @param keyListName The name of the list of keys for the fields to include
   *          on the form.
   * @param locale The locale.
   * @return The generated form.
   */
  @SuppressWarnings("unchecked")
  public <F extends Form> F createForm(
    final Object object,
    final String keyListName,
    final Locale locale) {
    final HtmlUiBuilderObjectForm form = createForm(object, getTypeName(),
      getKeyList(keyListName), locale);
    return (F)form;
  }

  @SuppressWarnings("unchecked")
  public <F extends Form> F createForm(
    final Object object,
    final String formName,
    final String keyList,
    final Locale locale) {
    final List<String> keys = getKeyList(keyList);

    return (F)createForm(object, formName, keys, locale);
  }

  protected T createObject() {
    return null;
  }

  public Element createObjectAddPage(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Map<String, Object> defaultValues,
    final String prefix) throws IOException, ServletException {
    final T object = createObject();

    JavaBeanUtil.setProperties(object, defaultValues);

    // if (!canAddObject(request)) {
    // response.sendError(HttpServletResponse.SC_FORBIDDEN,
    // "No permission to edit " + getTypeName() + " #" + getId());
    // return null;
    // }
    final Map<String, Object> parameters = new HashMap<String, Object>();

    final String pageName = getName(prefix, "add");
    final Set<String> parameterNamesToSave = new HashSet<String>();

    final Form form = createForm(object, pageName, request.getLocale());
    for (final String param : parameterNamesToSave) {
      form.addSavedParameter(param, request.getParameter(param));
    }
    form.initialize(request);

    if (form.isPosted() && form.isMainFormTask()) {
      if (form.isValid()) {
        if (preInsert(form, object)) {
          insertObject(object);
          parameters.put("message", "Saved");
          final Object id = getValue(object, getIdPropertyName());
          parameters.put(getIdParameterName(), id);

          postInsert(object);
          final String viewName = getName(prefix, "view");
          final String url = getPageUrl(viewName, parameters);
          response.sendRedirect(url);
          return null;
        }
      }
    }

    final Page page = getPage(pageName);
    final String title = page.getExpandedTitle();
    request.setAttribute("title", title);
    request.setAttribute("pageHeading", title);

    final Menu actionMenu = new Menu();
    addMenuItem(actionMenu, prefix, "list", "Cancel", "_top");
    addMenuItem(actionMenu, prefix, "add", "Clear Fields");
    actionMenu.addMenuItem(new Menu("Save", "javascript:document.forms['"
      + form.getName() + "'].submit()"));

    final MenuElement actionMenuElement = new MenuElement(actionMenu,
      "actionMenu");
    final ElementContainer view = new ElementContainer(form, actionMenuElement);
    return view;
  }

  public Element createObjectEditPage(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final T object,
    final String prefix) throws IOException, ServletException {
    if (object == null) {
      throw new NoSuchRequestHandlingMethodException(request);
    } else {
      // if (!canEditObject(request, object)) {
      // response.sendError(HttpServletResponse.SC_FORBIDDEN,
      // "No permission to edit " + getTypeName() + " #" + getId());
      // return null;
      // }
      final Map<String, Object> parameters = new HashMap<String, Object>();
      final Object id = getValue(object, getIdPropertyName());
      parameters.put(getIdParameterName(), id);

      final Set<String> parameterNamesToSave = new HashSet<String>();
      parameterNamesToSave.add(getIdParameterName());

      final String pageName = getName(prefix, "edit");
      final Form form = createForm(object, pageName, request.getLocale());
      for (final String param : parameterNamesToSave) {
        form.addSavedParameter(param, request.getParameter(param));
      }
      form.initialize(request);

      final String viewName = getName(prefix, "view");
      if (form.isPosted() && form.isMainFormTask()) {
        if (form.isValid() && preUpdate(form, object)) {
          updateObject(object);
          postUpdate(object);
          parameters.put("message", "Saved");

          final String url = getPageUrl(viewName, parameters);
          response.sendRedirect(url);
          return null;
        } else {
          setRollbackOnly(object);
        }
      } else {
        setRollbackOnly(object);
      }

      final Page page = getPage(pageName);

      final String title = page.getExpandedTitle();
      request.setAttribute("title", title);
      request.setAttribute("pageHeading", title);

      final Menu actionMenu = new Menu();
      addMenuItem(actionMenu, prefix, "view", "Cancel", "_top");
      addMenuItem(actionMenu, prefix, "edit", "Revert to Saved", "_top");
      actionMenu.addMenuItem(new Menu("Save", "javascript:document.forms['"
        + form.getName() + "'].submit()"));

      final MenuElement actionMenuElement = new MenuElement(actionMenu,
        "actionMenu");
      final ElementContainer view = new ElementContainer(form,
        actionMenuElement);
      return view;
    }
  }

  public ElementContainer createObjectListPage(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Map<String, Object> filter) throws IOException {
    final String prefix = null;
    final String pageName = "list";
    final FilterableTableView listView = createFilterableTableView(
      "objectList", null, pageName, request.getLocale(), true);
    final ElementContainer listContainer = new ElementContainer();
    listContainer.add(listView);

    final Form searchForm = new Form("searchForm");
    searchForm.setMethod("get");
    searchForm.add(listContainer);
    searchForm.initialize(request);

    final Map<String, Object> whereClause = new LinkedHashMap<String, Object>();
    if (searchForm.isValid()) {
      final Field idField = searchForm.getField("idLink");
      if (idField != null) {
        final Object id = searchForm.getValue("idLink");
        Object object = null;
        try {
          object = loadObject(id);
        } catch (final Exception e) {
        }
        if (object == null) {

          final String title = getTitle();
          idField.addValidationError(title + " #" + id + " not found");
        } else {
          final String idLinkUrl = getPageUrl("view",
            Collections.singletonMap("id", id));
          response.sendRedirect(idLinkUrl);
          return null;
        }
      }
      for (final String propertyName : searchForm.getFieldNames()) {
        if (!"search".equals(propertyName) && !"idLink".equals(propertyName)
          && searchForm.hasValue(propertyName)) {
          final Object value = searchForm.getValue(propertyName);
          if (value instanceof String) {
            final String string = (String)value;
            whereClause.put(propertyName, "%" + string + "%");
          } else {
            whereClause.put(propertyName, value);
          }
        }
      }
    }

    final ResultPager<T> pager = getResultPager(whereClause);
    updateObjectListView(request, listContainer, listView, pager);

    final ElementContainer view = new ElementContainer();
    view.add(searchForm);

    final Page page = getPage(pageName);
    final String title = page.getExpandedTitle();
    request.setAttribute("title", title);
    request.setAttribute("pageHeading", title);

    final Menu actionMenu = new Menu();
    actionMenu.addMenuItem(new Menu("Search", "#",
      "document.forms['searchForm'].submit(); return false;"));
    addMenuItem(actionMenu, prefix, pageName, "Clear Search");
    addMenuItem(actionMenu, prefix, "add", "Add", "_top");
    addMenuElement(view, actionMenu);
    return view;
  }

  public Element createObjectListPage(
    final HttpServletRequest request,
    final List<T> results,
    final String prefix) {
    final String pageName = getName(prefix, "list");
    final ElementContainer tableView = createTableView(results, pageName, null,
      request.getLocale());
    String title = getPageTitle(pageName);
    tableView.setDecorator(new CollapsibleBox(title, true));

    final ElementContainer container = new ElementContainer();
    container.add(tableView);

    setTitleAttribute(request, pageName);

    final Menu actionMenu = new Menu();
    addMenuItem(actionMenu, prefix, "add", "Add", "_top");
    addMenuElement(container, actionMenu);

    return container;
  }

  public Element createObjectListPage(
    final HttpServletRequest request,
    final ResultPager<T> pager,
    final String prefix) {
    final String pageName = getName(prefix, "list");
    final ElementContainer container = new ElementContainer();
    final List<String> keyListkeyList = getKeyList(pageName);
    final FilterableTableView tableView = createFilterableTableView(
      "objectList", null, keyListkeyList, request.getLocale(), false);
    container.add(tableView);
    updateObjectListView(request, container, tableView, pager);

    setPageTitleAttribute(request, pageName);

    final Menu actionMenu = new Menu();
    addMenuItem(actionMenu, prefix, "add", "Add", "_top");
    addMenuElement(container, actionMenu);

    return container;
  }

  public ElementContainer createObjectViewPage(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Object object,
    final String prefix) throws NoSuchRequestHandlingMethodException {
    if (object == null) {
      throw new NoSuchRequestHandlingMethodException(request);
    } else {
      final String pageName = getName(prefix, "view");

      final Page page = getPage(pageName);
      List<String> keyList = getKeyList(pageName, "view");
      final Element detailView = createDetailView(object, "objectView",
        keyList, request.getLocale());
      detailView.setDecorator(new CollapsibleBox("Details", true));

      final String title = page.getExpandedTitle();
      request.setAttribute("title", title);
      request.setAttribute("pageHeading", title);

      final Menu actionMenu = new Menu();
      addMenuItem(actionMenu, prefix, "edit", "Edit", "_top");

      final ElementContainer view = new ElementContainer(detailView);
      addMenuElement(view, actionMenu);
      return view;
    }
  }

  public ElementContainer createTableView(
    final Collection<?> rows,
    final String cssClass,
    final String title,
    final String keyListName,
    final String noRecordsMessage,
    final Locale locale) {
    final List<String> keys = getKeyList(keyListName);
    final HtmlUiBuilderCollectionTableSerializer model = new HtmlUiBuilderCollectionTableSerializer(
      this, keys, locale);
    model.setRows(rows);
    TableView tableView = new TableView(model, cssClass + " " + typeName,
      title, noRecordsMessage);
    return new ElementContainer(tableView);
  }

  public <V> ElementContainer createTableView(
    final Collection<V> results,
    final String keyListName,
    final String noRecordsMessage,
    final Locale locale) {
    return createTableView(results, "objectList", null, keyListName,
      noRecordsMessage, locale);
  }

  /**
   * @return Returns the beanFactory.
   */
  protected final BeanFactory getBeanFactory() {
    return beanFactory;
  }

  /**
   * Get the HTML UI Builder for the object's class.
   * 
   * @param objectClass<?> The object class.
   * @return The builder.
   */
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <H extends HtmlUiBuilder<?>> H getBuilder(final Class<?> objectClass) {
    if (builderFactory != null) {
      return builderFactory.get(objectClass);
    } else {
      final HtmlUiBuilder htmlUiBuilder = HtmlUiBuilderFactory.get(beanFactory,
        objectClass);
      return (H)htmlUiBuilder;
    }
  }

  /**
   * Get the HTML UI Builder for the object's class.
   * 
   * @param object The object.
   * @return The builder.
   */
  @SuppressWarnings("unchecked")
  public <H extends HtmlUiBuilder<?>> H getBuilder(final Object object) {
    if (object != null) {
      final Class<?> class1 = object.getClass();
      @SuppressWarnings("rawtypes")
      final HtmlUiBuilder builder = getBuilder(class1);
      return (H)builder;
    } else {
      return null;
    }
  }

  /**
   * Get the HTML UI Builder for the class.
   * 
   * @param className The name of the class.
   * @return The builder.
   */
  @SuppressWarnings("unchecked")
  public <H extends HtmlUiBuilder<?>> H getBuilder(final String typeName) {
    @SuppressWarnings("rawtypes")
    final HtmlUiBuilder htmlUiBuilder = HtmlUiBuilderFactory.get(beanFactory,
      typeName);
    return (H)htmlUiBuilder;
  }

  /**
   * Get the factory used to get related HTML UI builders,
   * 
   * @return The factory.
   */
  public HtmlUiBuilderFactory getBuilderFactory() {
    return builderFactory;
  }

  /**
   * Get the Data Access Object for the object's class.
   * 
   * @param objectClass<?> The object class.
   * @return The builder.
   */
  public <V> DataAccessObject<V> getDao(final Class<?> objectClass) {
    if (beanFactory != null) {
      return SpringDaoFactory.get(beanFactory, objectClass);
    } else {
      return null;
    }
  }

  /**
   * Get the Data Access Object for the object's class name.
   * 
   * @param objectClass<?>Name The object class name.
   * @return The builder.
   */
  public <V> DataAccessObject<V> getDao(final String objectClassName) {
    if (beanFactory != null) {
      return SpringDaoFactory.get(beanFactory, objectClassName);
    } else {
      return null;
    }
  }

  public int getDefaultPageSize() {
    return defaultPageSize;
  }

  /**
   * Create a new field (or element) for the named key. The parameters from the
   * HttpRequest can be used to customise the look of the field.
   * 
   * @param request The servlet request.
   * @param key The field key.
   * @return The generated field element.
   */
  public Element getField(final HttpServletRequest request, final String key) {
    if (key.equals("id")) {
      return new LongField("id", false);
    } else if (fields.containsKey(key)) {
      final Element field = fields.get(key);
      return field.clone();
    } else {
      final TextField field = new TextField(key, false);
      return field;
    }
  }

  public String getFieldInstruction(final String key) {
    return fieldInstructions.get(key);
  }

  public Map<String, String> getFieldInstructions() {
    return fieldInstructions;
  }

  public Decorator getFieldLabel(final String key, final boolean field) {
    final Map<String, Decorator> fieldLabels = getFieldLabels();
    Decorator fieldLabel = fieldLabels.get(key);
    if (fieldLabel == null) {
      final String label = getLabel(key);
      final String instructions = getFieldInstruction(key);
      if (field) {
        fieldLabel = new FieldLabelDecorator(label, instructions);
      } else {
        fieldLabel = new ElementLabel(label, instructions);
      }
      fieldLabels.put(key, fieldLabel);
    }
    return fieldLabel;
  }

  public Map<String, Decorator> getFieldLabels() {
    return fieldLabels;
  }

  public Map<String, Element> getFields() {
    return fields;
  }

  /**
   * @return Returns the idParameterName.
   */
  public String getIdParameterName() {
    return idParameterName;
  }

  public String getIdPropertyName() {
    return idPropertyName;
  }

  public Object getIdValue(final Object object) {
    return getValue(object, idPropertyName);
  }

  /**
   * Get the key list with the specified name, or the default if not defined.
   * 
   * @param name The name of the key list.
   * @return The key list.
   */
  public List<String> getKeyList(final String name) {
    return getKeyListOrDefault(keyLists, name, "default");
  }

  /**
   * Get the key list with the specified name, or the list for defaultName if
   * not defined.
   * 
   * @param name The name of the key list.
   * @param defaultName The name of the default key list to use.
   * @return The key list.
   */
  public List<String> getKeyList(final String name, final String defaultName) {
    return getKeyListOrDefault(keyLists, name, defaultName);
  }

  /**
   * Get the key list with the specified name, or the default if not defined.
   * 
   * @param keyLists The map of key lists.
   * @param name The name of the key list.
   * @param defaultName The name of the default key List
   * @return The key list.
   */
  private List<String> getKeyListOrDefault(
    final Map<String, List<String>> keyLists,
    final String name,
    final String defaultName) {
    List<String> keyList = keyLists.get(name);
    if (keyList == null) {
      keyList = keyLists.get(defaultName);
      if (keyList == null) {
        return Collections.emptyList();
      }
    }
    return keyList;
  }

  /**
   * Get the map of key lists.
   * 
   * @return The map of key lists.
   */
  public Map<String, List<String>> getKeyLists() {
    return keyLists;
  }

  /**
   * @return Returns the keySerializers.
   */
  public Map<String, KeySerializer> getKeySerializers() {
    return keySerializers;
  }

  /**
   * <p>
   * Get the label for the key in the default {@link Locale}.
   * </p>
   * 
   * @param key The key.
   * @return The label.
   * @see #getLabel(String, Locale)
   */
  public String getLabel(final String key) {
    return getLabel(key, Locale.getDefault());
  }

  /**
   * <p>
   * Get the label for the key in the specified {@link Locale}. The following
   * process is used (in sequence) to get the label for the key.
   * </p>
   * <ol>
   * <li>An explict label defined in {@link #setLabels(Map)}</li>
   * <li>The label for the propetry name portion of a sub key (e.g. For the key
   * "organization.name" the property name portion is "organization", so the
   * label would be the label for the key "organization").</li>
   * <li>The label for the link text key of a link key (e.g. For the key
   * "Link(view, id)" the link text key is "id", so the label would be the label
   * for the key "id"</li>
   * <li>The key converted to Upper Case Words.</li>
   * </ol>
   * <p>
   * After the first call for a particular key the calculated labels are cached.
   * </p>
   * 
   * @param key The key.
   * @param locale The locale.
   * @return The label.
   */
  public String getLabel(final String key, final Locale locale) {
    String label = getLabels().get(key);
    if (label == null) {
      final Matcher linkKeyMatcher = LINK_KEY_PATTERN.matcher(key);
      if (linkKeyMatcher.find()) {
        label = getLabel(linkKeyMatcher.group(2), locale);
      } else {
        final Matcher subKeyMatcher = SUB_KEY_PATTERN.matcher(key);
        if (subKeyMatcher.find() && subKeyMatcher.group(2) != null) {
          label = getLabel(subKeyMatcher.group(1));
        } else {
          label = CaseConverter.toCapitalizedWords(key);
        }
      }

      labels.put(key, label);
    }
    return label;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public int getMaxPageSize() {
    return maxPageSize;
  }

  public String getMessage(final String messageName) {
    return messages.get(messageName);
  }

  public String getMessage(
    final String messageName,
    final Map<String, Object> variables) {
    final String message = getMessage(messageName);
    if (message != null) {
      try {
        final Expression expression = JexlUtil.createExpression(message);
        if (expression != null) {
          final JexlContext context = new HashMapContext();
          context.setVars(variables);
          return (String)expression.evaluate(context);
        }
      } catch (final Throwable e) {
        log.error(e.getMessage(), e);
      }
    }
    return message;
  }

  private String getName(final String prefix, final String keyListName) {
    if (StringUtils.hasText(prefix)) {
      return prefix + CaseConverter.toUpperFirstChar(keyListName);
    } else {
      return keyListName;
    }
  }

  private String getNullLabel(final String key) {
    return nullLabels.get(key);
  }

  /**
   * @return Returns the nullLabels.
   */
  public Map<String, String> getNullLabels() {
    return nullLabels;
  }

  public Map<String, Boolean> getOrderBy() {
    synchronized (orderBy) {
      if (orderBy.isEmpty()) {
        orderBy.put(getIdPropertyName(), true);
      }
    }
    return orderBy;
  }

  public Page getPage(final String path) {
    Page linkPage = pagesByName.get(path);
    if (linkPage == null) {
      final WebUiContext webUiContext = WebUiContext.get();
      if (webUiContext != null) {
        final Page page = webUiContext.getPage();
        if (page != null) {
          linkPage = page.getPage(path);
        }
      }
      if (linkPage == null) {
        final String pageByName = pageUrls.get(path);
        if (pageByName != null) {
          linkPage = new Page(null, getPluralTitle(), pageByName, false);
        } else {
          linkPage = new Page(null, getPluralTitle(), path, false);
        }
      }
    }
    return linkPage;
  }

  public Page getPage(final String prefix, final String name) {
    final String pageName = getName(prefix, name);
    final Page page = getPage(pageName);
    return page;
  }

  public Map<String, Page> getPagesByName() {
    return pagesByName;
  }

  private String getPageTile() {
    final HttpServletRequest request = getRequest();
    return (String)request.getAttribute("title");
  }

  public String getPageUrl(final String name) {
    final String url = pageUrls.get(name);
    return url;
  }

  public String getPageUrl(
    final String name,
    final Map<String, ? extends Object> parameters) {
    final Page page = getPage(name);
    if (page == null) {
      return null;
    } else {
      final String url = page.getFullUrl(parameters);
      return url;
    }
  }

  public Map<String, String> getPageUrls() {
    return pageUrls;
  }

  public String getPluralTitle() {
    return pluralTitle;
  }

  public ResultPager<T> getResultPager(final Map<String, Object> filter) {
    throw new UnsupportedOperationException();
  }

  public String getTitle() {
    return title;
  }

  public String getTypeName() {
    return typeName;
  }

  public Object getValue(final Object object, final String key) {
    if (object instanceof DataObject) {
      final DataObject dataObject = (DataObject)object;
      return dataObject.getValueByPath(key);
    } else if (object instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, ?> map = (Map<String, ?>)object;
      return map.get(key);
    } else {
      return JavaBeanUtil.getProperty(object, key);
    }
  }

  public boolean hasPageUrl(final String pageName) {
    return pageUrls.containsKey(pageName);
  }

  public void idLink(final XmlWriter out, final Object object) {
    final Object id = getIdValue(object);
    if (id != null) {
      final Map<String, Object> parameters = Collections.singletonMap(
        idParameterName, id);
      final String url = getPageUrl("view", parameters);
      HtmlUtil.serializeA(out, null, url, id);
    }
  }

  public void initializeForm(
    final HtmlUiBuilderObjectForm form,
    final HttpServletRequest request) {
  }

  protected void insertObject(final T object) {
  }

  public boolean isUsePathVariables() {
    return usePathVariables;
  }

  public T loadObject(final Object id) {
    throw new UnsupportedOperationException();
  }

  protected void postInsert(final T object) {
  }

  protected void postUpdate(final T object) {
  }

  protected boolean preInsert(final Form form, final T object) {
    return true;
  }

  protected boolean preUpdate(final Form form, final T object) {
    return true;
  }

  /**
   * Serialize the value represented by the key from the object. The locale can
   * be used to customize the display for that locale (e.g. different date
   * formats).
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key to serialize.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serialize(
    final XmlWriter out,
    final Object object,
    final String key,
    final Locale locale) {

    if (object == null) {
      serializeNullLabel(out, key, locale);
    } else {
      final Object serializer = keySerializers.get(key);
      if (serializer == null) {
        String path = null;
        String valueKey = key;
        final Matcher linkMatcher = LINK_KEY_PATTERN.matcher(key);

        if (linkMatcher.matches()) {
          path = linkMatcher.group(1);
          valueKey = linkMatcher.group(2);
        }

        HtmlUiBuilder<? extends Object> uiBuilder = this;
        final String[] parts = valueKey.split("\\.");
        Object currentObject = object;
        for (int i = 0; i < parts.length - 1; i++) {
          final String keyName = parts[i];
          try {
            currentObject = getValue(currentObject, keyName);
            if (currentObject == null) {
              serializeNullLabel(out, keyName, locale);
              return;
            }

            uiBuilder = getBuilder(currentObject);
          } catch (final IllegalArgumentException e) {
            final String message = currentObject.getClass().getName()
              + " does not have a property " + keyName;
            log.error(e.getMessage(), e);
            out.element(HtmlUtil.B, message);
            return;
          }
        }
        final String lastKey = parts[parts.length - 1];
        if (path == null) {
          if (uiBuilder == this) {
            try {
              final Object value = getValue(currentObject, lastKey);
              if (value == null) {
                serializeNullLabel(out, lastKey, locale);
                return;
              } else {
                final TypeSerializer typeSerializer = classSerializers.get(value.getClass());
                if (typeSerializer == null) {
                  final String stringValue = value.toString();
                  if (stringValue.length() > 0) {
                    out.text(stringValue);
                    return;
                  }
                } else {
                  typeSerializer.serialize(out, value, locale);
                  return;
                }
              }
            } catch (final IllegalArgumentException e) {
              final String message = currentObject.getClass().getName()
                + " does not have a property " + key;
              log.error(e.getMessage(), e);
              out.element(HtmlUtil.B, message);
              return;
            }
          } else {
            uiBuilder.serialize(out, currentObject, lastKey, locale);

          }
        } else {
          uiBuilder.serializeLink(out, currentObject, lastKey, path, locale);
        }
      } else {
        if (serializer instanceof TypeSerializer) {
          final TypeSerializer typeSerializer = (TypeSerializer)serializer;
          typeSerializer.serialize(out, object, locale);
          return;
        } else if (serializer instanceof KeySerializer) {
          final KeySerializer keySerializer = (KeySerializer)serializer;
          keySerializer.serialize(out, object, key, locale);
          return;
        }

      }
    }
  }

  public void serializeIdLink(
    final XmlWriter out,
    final String path,
    final Object id) {
    final Map<String, Object> parameters = Collections.singletonMap(
      idParameterName, id);
    final String url = getPageUrl(path, parameters);
    if (url != null) {
      HtmlUtil.serializeA(out, null, url, id);
    }
  }

  protected void serializeLink(
    final XmlWriter out,
    final Object object,
    final String key,
    final String path,
    final Locale locale) {
    final Object id = getIdValue(object);
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(idParameterName, id);
    final String url = getPageUrl(path, parameters);
    if (url != null) {
      out.startTag(HtmlUtil.A);
      out.attribute(HtmlUtil.ATTR_HREF, url);
      out.attribute(HtmlUtil.ATTR_TARGET, "_top");
      serialize(out, object, key, locale);
      out.endTag(HtmlUtil.A);
    }
  }

  public void serializeLink(
    final XmlWriter out,
    final String pageName,
    final Object object) {
    final Object id = getIdValue(object);
    final Map<String, Object> parameters = Collections.singletonMap(
      idParameterName, id);
    final String url = getPageUrl(pageName, parameters);
    if (url != null) {
      HtmlUtil.serializeA(out, null, url, pageName);
    }
  }

  /**
   * Serialize the message where a key has no value. The default is the
   * character '-'.
   * 
   * @param out The XML writer to serialize to.
   * @param key The key to serialize the no value message for.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serializeNullLabel(
    final XmlWriter out,
    final String key,
    final Locale locale) {
    final String nullLabel = getNullLabel(key);
    if (nullLabel == null) {
      final int dotIndex = key.lastIndexOf('.');
      if (dotIndex == -1) {
        out.text("-");
      } else {
        serializeNullLabel(out, key.substring(0, dotIndex), locale);
      }
    } else {
      out.text(nullLabel);
    }
  }

  public void setBeanFactory(final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Set the factory used to get related HTML UI builders,
   * 
   * @param builderFactory The factory.
   */
  public void setBuilderFactory(final HtmlUiBuilderFactory builderFactory) {
    this.builderFactory = builderFactory;
  }

  public void setDefaultPageSize(final int defaultPageSize) {
    this.defaultPageSize = defaultPageSize;
  }

  /**
   * @param fieldInstructions The fieldInstructions to set.
   */
  public void setFieldInstructions(final Map<String, String> fieldInstructions) {
    this.fieldInstructions = fieldInstructions;
  }

  public void setFields(final Map<String, Element> fields) {
    this.fields = fields;
  }

  /**
   * @param idParameterName The idParameterName to set.
   */
  public void setIdParameterName(final String idParameterName) {
    this.idParameterName = idParameterName;
  }

  public void setIdPropertyName(final String idPropertyName) {
    this.idPropertyName = idPropertyName;
  }

  /**
   * Set the key list with the specified name.
   * 
   * @param name The name of the key list.
   * @param keyList<String> The key list.
   */
  public void setKeyList(final String name, final List<String> keyList) {
    keyLists.put(name, keyList);
  }

  /**
   * Set the map of key lists.
   * 
   * @param keyLists The map of key lists.
   */
  public void setKeyLists(final Map<String, List<String>> keyLists) {
    this.keyLists = keyLists;
    if (!keyLists.containsKey("list")) {
      setKeyList("list", getKeyList("listView"));
    }
    if (!keyLists.containsKey("detail")) {
      setKeyList("detail", getKeyList("detailView"));
    }
    if (!keyLists.containsKey("form")) {
      setKeyList("form", getKeyList("formView"));
    }
  }

  /**
   * @param keySerializers The keySerializers to set.
   */
  public void setKeySerializers(final Map<String, KeySerializer> keySerializers) {
    this.keySerializers = keySerializers;
  }

  /**
   * @param labels The labels to set.
   */
  public void setLabels(final Map<String, String> labels) {
    this.labels = labels;
  }

  public void setMaxPageSize(final int maxPageSize) {
    this.maxPageSize = maxPageSize;
  }

  public void setMessages(final Map<String, String> messages) {
    this.messages = messages;
  }

  /**
   * @param nullLabels The nullLabels to set.
   */
  public void setNullLabels(final Map<String, String> nullLabels) {
    this.nullLabels = nullLabels;
  }

  public void setOrderBy(final Map<String, Boolean> orderBy) {
    this.orderBy = orderBy;
  }

  public void setPages(final Collection<Page> pages) {
    for (final Page page : pages) {
      pagesByName.put(page.getName(), page);
    }
  }

  public void setPagesByName(final Map<String, Page> pagesByName) {
    this.pagesByName = pagesByName;
  }

  public void setPageTitleAttribute(
    final HttpServletRequest request,
    final String pageName) {
    final Page page = getPage(pageName);
    if (page != null) {
      final String title = page.getExpandedTitle();
      request.setAttribute("title", title);
      request.setAttribute("pageHeading", title);
    }
  }

  public String getPageTitle(final String pageName) {
    final Page page = getPage(pageName);
    if (page == null) {
      return null;
    } else {
      final String title = page.getExpandedTitle();
      return title;
    }
  }

  public void setTitleAttribute(
    final HttpServletRequest request,
    final String pageName) {
    final String title = getPageTitle(pageName);
    request.setAttribute("title", title);
  }

  public void setPageUrls(final Map<String, String> pageUrls) {
    this.pageUrls = pageUrls;
  }

  public void setPluralTitle(final String pluralTitle) {
    this.pluralTitle = pluralTitle;
  }

  public void setRollbackOnly(final T object) {
  }

  public void setServletContext(final ServletContext servletContext) {
  }

  public void setTitle(final String typeLabel) {
    this.title = typeLabel;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
    if (idParameterName == null) {
      this.idParameterName = typeName + "Id";
    }
  }

  public void setUsePathVariables(final boolean usePathVariables) {
    this.usePathVariables = usePathVariables;
  }

  public void setValue(final Object object, final String key, final Object value) {
    if (object instanceof DataObject) {
      final DataObject dataObject = (DataObject)object;
      dataObject.setValueByPath(key, value);
    } else if (object instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Object> map = (Map<String, Object>)object;
      map.put(key, value);
    } else {
      JavaBeanUtil.setProperty(object, key, value);
    }
  }

  protected void updateObject(final T object) {
  }

  private void updateObjectListView(
    final HttpServletRequest request,
    final ElementContainer listContainer,
    final FilterableTableView listView,
    final ResultPager<T> pager) {
    try {

      List<?> results = Collections.emptyList();
      int pageSize;
      try {
        pageSize = Integer.parseInt(request.getParameter("pageSize"));
      } catch (final Throwable t) {
        pageSize = defaultPageSize;
      }
      pager.setPageSize(Math.min(maxPageSize, pageSize));
      try {
        final String page = request.getParameter("page");
        pager.setPageNumber(Integer.parseInt(page));
      } catch (final Throwable t) {
        pager.setPageNumber(1);
      }
      results = pager.getList();

      listView.setRows(results);

      if (pager.getNumResults() > 0) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> parameters = request.getParameterMap();
        final ResultPagerView pagerView = new ResultPagerView(pager,
          request.getRequestURI(), parameters);
        listContainer.add(0, pagerView);
        listContainer.add(pagerView);
      }
    } finally {
      pager.close();
    }
  }

  public boolean validateForm(final HtmlUiBuilderObjectForm form) {
    return true;
  }
}
