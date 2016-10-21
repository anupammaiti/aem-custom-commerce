package org.test.commerceexample.core.commerce;
 
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
  
import com.adobe.cq.commerce.common.AbstractJcrProduct;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import io.sphere.sdk.products.Price;
import io.sphere.sdk.products.ProductProjection;
import io.sphere.sdk.products.queries.ProductProjectionQuery;
import io.sphere.sdk.queries.PagedQueryResult;

import javax.money.MonetaryAmount;
import java.util.List;

public class TrainingProductImpl extends AbstractJcrProduct {
    public static final String PN_IDENTIFIER = "identifier";
    public static final String PN_PRICE = "price";
  
    protected final ResourceResolver resourceResolver;
    protected final PageManager pageManager;
    protected final Page productPage;
    protected String brand = null;
  
    public TrainingProductImpl(Resource resource) {
        super(resource);
  
        resourceResolver = resource.getResourceResolver();
        pageManager = resourceResolver.adaptTo(PageManager.class);
        productPage = pageManager.getContainingPage(resource);
        /*this.pageManager = ((PageManager)this.resourceResolver.adaptTo(PageManager.class));
        this.productPage = this.pageManager.getContainingPage(resource);*/
    }
  
    public String getSKU() {
        final String projectKey = "PROJECT";
        final String clientId = "CLIENT_ID";
        final String clientSecret = "CLIENT_SECRET";
        final SphereClientConfig clientConfig = SphereClientConfig.of(projectKey, clientId, clientSecret);
        final SphereClient client = SphereClientFactory.of().createClient(clientConfig);

        // fixed SKU for testing
        // https://admin.sphere.io/anthony-sandbox-17/products/b8fea737-c00f-49c8-bc7c-b22bc9ef4d76/stock
        final String sku = "sku_MB_PREMIUM_TECH_T_variant1_1473879291150";

        // query by SKU
        final PagedQueryResult<ProductProjection> queryResult = client.execute(ProductProjectionQuery.ofCurrent()
                .withPredicates(product -> product.allVariants().where(variant -> variant.sku().is(sku)))).toCompletableFuture().join();

        // log how many products received
        int productProjectionSize = queryResult.getResults().size();
        System.out.println("Payload size is: " + productProjectionSize);

        // exit if no products are returned
        if (productProjectionSize == 0) {
          System.out.println("No Results found");
          System.exit(0);
        }

        // get first product
        final ProductProjection productProjection = queryResult.getResults().get(0);
        System.out.println("Response: " + productProjection);

        // ensure correct product
        final String productId = productProjection.getId();
        System.out.println("ProdutId: " + productId);

        // get list of prices for a MasterVariant
        List<Price> prices = productProjection.getMasterVariant().getPrices();

        for(Price price : prices) {
          MonetaryAmount value = price.getValue();
          System.out.println("Price is set to: " + value);
        }
        return prices[0].getValue();
    }
}