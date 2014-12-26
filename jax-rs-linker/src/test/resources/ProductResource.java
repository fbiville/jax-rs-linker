package net.biville.florent.jax_rs_linker.parser;

import net.biville.florent.jax_rs_linker.api.Self;
import net.biville.florent.jax_rs_linker.api.SubResource;

import net.biville.florent.jax_rs_linker.parser.BrandResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/product")
public class ProductResource {

    @Self
    @Path("/{id}")
    @GET
    public void getById(@PathParam("id") int id) {

    }

    @SubResource(BrandResource.class)
    @Path("/{id}/brand")
    @GET
    public void getBrandByProductId(@PathParam("id") int productId) {

    }
}
