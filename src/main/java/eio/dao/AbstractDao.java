package eio.dao;

import eio.domain.ExposableId;
import eio.exception.ObjectNotFoundException;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public abstract class AbstractDao<T extends Serializable> {
	@PersistenceContext
	private EntityManager entityManager;

	public void store(T item) {
		if (item instanceof ExposableId) {
			((ExposableId) item).setExposableId(generateExposableId());
		}
		try {
			UserTransaction tx = getBeginTransaction();
			getEntityManager().persist(item);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(T item) {
		try {
			UserTransaction tx = getBeginTransaction();
			getEntityManager().merge(item);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private UserTransaction getBeginTransaction() {
		try {
			UserTransaction tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			tx.begin();
			return tx;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public T get(Long id) throws ObjectNotFoundException {
		if (id == null) {
			throw new ObjectNotFoundException();
		}
		T item = getEntityManager().find(getGenericClass(), id);
		if (item == null) {
			throw new ObjectNotFoundException();
		}
		getEntityManager().detach(item);
		return item;
	}

	public T get(String id) throws ObjectNotFoundException {
		if (id == null) {
			throw new ObjectNotFoundException();
		}
		return get(id, getGenericClass().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	protected T get(String id, String className) throws ObjectNotFoundException {
		Query query = getEntityManager().createQuery("SELECT i FROM " + className + " i WHERE i.exposableId = :id");
		query.setParameter("id", id);
		List<T> list = query.getResultList();
		if (list.isEmpty()) {
			throw new ObjectNotFoundException();
		}
		getEntityManager().detach(list.get(0));
		return list.get(0);
	}

	public void delete(T item) {
		item = getEntityManager().merge(item);
		getEntityManager().remove(item);
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	protected void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	protected String generateExposableId() {
		return generateExposableId(getGenericClass().getSimpleName());
	}

	protected String generateExposableId(String className) {
		String result;
		do {
			result = UUID.randomUUID().toString();
            try {
                get(result, className);
                result = "";
            } catch (ObjectNotFoundException e) {
				// Success
            }
		} while (result.isEmpty());
		return result;
	}

	@SuppressWarnings("unchecked")
	private Class<T> getGenericClass() {
		Class<T> result = null;
		Type type = this.getClass().getGenericSuperclass();

		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] fieldArgTypes = pt.getActualTypeArguments();
			result = (Class<T>) fieldArgTypes[0];
		}

		return result;
	}

	protected <E extends Serializable> List<E> detachAll(List<E> list) {
		EntityManager em = getEntityManager();
		for (Object o : list) {
			try {
				em.detach(o);
			} catch (IllegalArgumentException e) {
				// do nothing
			}
		}
		return list;
	}
}
