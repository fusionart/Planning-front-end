package com.monbat.components.base_page;

import com.monbat.components.center_area.CenterArea;
import com.monbat.components.footer.Footer;
import com.monbat.components.header.Header;
import com.monbat.components.menubar.MenuBar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import static com.monbat.components.ComponentIds.*;

public class BasePage extends WebPage {
    public BasePage() {
        // Add header
        add(new Header(HEADER));

        // Add menu bar
        add(new MenuBar(MENU_BAR));

        // Add center area (default empty or with a placeholder)
        add(new CenterArea(CENTER_AREA));

        // Add footer
        add(new Footer(FOOTER));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        // Add local jQuery and jQuery UI
        response.render(JavaScriptHeaderItem.forReference(
                new JavaScriptResourceReference(BasePage.class, "js/jquery-3.7.1.min.js")));
        response.render(JavaScriptHeaderItem.forReference(
                new JavaScriptResourceReference(BasePage.class, "js/jquery-ui.min.js")));
        response.render(CssHeaderItem.forReference(
                new CssResourceReference(BasePage.class, "css/jquery-ui.min.css")));

        // Add Bootstrap CSS
        response.render(CssHeaderItem.forReference(
                new CssResourceReference(BasePage.class, "css/bootstrap.min.css")));

        // Add Bootstrap JS
        response.render(JavaScriptHeaderItem.forReference(
                new JavaScriptResourceReference(BasePage.class, "js/bootstrap.bundle.min.js")));
    }
}
