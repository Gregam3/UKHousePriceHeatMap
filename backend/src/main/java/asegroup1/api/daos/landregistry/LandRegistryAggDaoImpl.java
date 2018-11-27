package asegroup1.api.daos.landregistry;

import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.PostCodeCoordinatesAgg;

public class LandRegistryAggDaoImpl extends DaoImpl<PostCodeCoordinatesAgg> {

	private static final String TABLE_NAME = "postcodelatlng_agg";

	public LandRegistryAggDaoImpl() {
		setCurrentClass(PostCodeCoordinatesAgg.class);
	}

}
