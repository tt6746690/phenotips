/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.data.internal;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiHibernateBaseStore;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.store.migration.DataMigrationException;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.store.migration.hibernate.AbstractHibernateDataMigration;

/**
 * Migration for PhenoTips issue #1378: Allow filtering by maximum privacy level in export (as or more public). This is
 * better implemented if all patients have a visibility object which until now they do not.
 *
 * @version $Id$
 * @since 1.1M1
 */
@Component
@Named("R54691PhenoTips#1378")
@Singleton
public class R54691PhenoTips1378DataMigration extends AbstractHibernateDataMigration
{
    /** Logging helper object. */
    @Inject
    private Logger logger;

    /** Resolves unprefixed document names to the current wiki. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    /** Serializes the class name without the wiki prefix, to be used in the database query. */
    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> serializer;

    @Override
    public String getDescription()
    {
        return "Adding a default 'private' visibility object to all patients which did not have a "
            + "visibility object #1378";
    }

    @Override
    public XWikiDBVersion getVersion()
    {
        return new XWikiDBVersion(54691);
    }

    @Override
    public void hibernateMigrate() throws DataMigrationException, XWikiException
    {
        getStore().executeWrite(getXWikiContext(), new AddVisObjectCallback());
    }

    /**
     * Searches for all documents containing vcf files and replaces the reference genome name with the GRCh notation.
     */
    private final class AddVisObjectCallback implements XWikiHibernateBaseStore.HibernateCallback<Object>
    {
        @Override
        public Object doInHibernate(Session session) throws HibernateException, XWikiException
        {

            XWikiContext context = R54691PhenoTips1378DataMigration.this.getXWikiContext();
            XWiki xwiki = context.getWiki();
            DocumentReference classReference =
                new DocumentReference(context.getDatabase(), "PhenoTips", "VisibilityClass");
            Query q1 =
                session
                    .createQuery("select doc.fullName from XWikiDocument as doc, BaseObject "
                    + "as patobj where doc.fullName=patobj.name "
                        + "and patobj.className='PhenoTips.PatientClass'");
            Query q2 =
                session
                    .createQuery("select doc.fullName from XWikiDocument as doc, BaseObject patobj, BaseObject visobj "
                    + "where doc.fullName=patobj.name and "
                        + "patobj.className='PhenoTips.PatientClass' and "
                        + "visobj.className='PhenoTips.VisibilityClass' and doc.fullName=visobj.name");
            @SuppressWarnings("unchecked")
            Set<String> documents = new HashSet<String>(q1.list());
            documents.removeAll(q2.list());
            R54691PhenoTips1378DataMigration.this.logger.debug("Found {} documents with no visibility object",
                documents.size());
            for (String docName : documents) {
                R54691PhenoTips1378DataMigration.this.logger.debug("Checking [{}]", docName);
                XWikiDocument doc =
                    xwiki.getDocument(R54691PhenoTips1378DataMigration.this.resolver.resolve(docName), context);
                BaseObject visobj = new BaseObject();
                visobj.setXClassReference(classReference);
                visobj.set("visibility", "private", context);
                doc.addXObject(visobj);

                doc.setComment(R54691PhenoTips1378DataMigration.this.getDescription());
                doc.setMinorEdit(true);
                try {
                    // There's a bug in XWiki which prevents saving an object in the same session that it was loaded,
                    // so we must clear the session cache first.
                    session.clear();
                    ((XWikiHibernateStore) getStore()).saveXWikiDoc(doc, context, false);
                    session.flush();
                    R54691PhenoTips1378DataMigration.this.logger.debug("Updated [{}]", docName);
                } catch (DataMigrationException e) {
                    // We're in the middle of a migration, we're not expecting another migration
                }
            }
            return null;
        }
    }
}
