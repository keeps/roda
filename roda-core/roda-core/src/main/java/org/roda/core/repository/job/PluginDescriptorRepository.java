/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.List;

import java.util.Optional;

import org.roda.core.data.v2.db.jobs.PluginDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Plugins referenced by job reports and their steps ({@code plugins}). Used through {@link JobDatabaseService}.
 *
 * @author RODA Development Team
 */
@Repository
public interface PluginDescriptorRepository extends JpaRepository<PluginDescriptor, Integer> {
  Optional<PluginDescriptor> findByClassNameAndVersion(String className, String version);
}
