package eio.service.rest;

import eio.dao.AbstractDao;
import eio.exception.InvalidInputParametersException;
import eio.exception.ObjectNotFoundException;
import eio.service.model.AbstractRestModel;
import org.apache.commons.validator.GenericValidator;

import java.io.Serializable;

public abstract class AbstractSetObjectService<I extends AbstractRestModel, O extends Serializable> {
    protected abstract void validateInputParameters(I model) throws InvalidInputParametersException;
    protected abstract O createObject(I model) throws ObjectNotFoundException;
    protected abstract void updateObject(O output, I model) throws ObjectNotFoundException;
    protected abstract AbstractDao<O> getObjectDao();

    protected O createUpdateObject(I model) throws ObjectNotFoundException {
        O object;
        if (!GenericValidator.isBlankOrNull(model.id)) {
            object = getObjectDao().get(model.id);
        } else {
            object = createObject(model);
        }
        updateObject(object, model);
        return object;
    }
}
