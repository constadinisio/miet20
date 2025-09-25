export async function up(knex) {
  const hasFiles = await knex.schema.hasTable('archivos');
  if (!hasFiles) {
    await knex.schema.createTable('archivos', (table) => {
      table.increments('id').primary();
      table.string('nombre_original', 255).notNullable();
      table.string('mime_type', 120).notNullable();
      table.bigInteger('tamanio_bytes').notNullable();
      table.string('ruta_storage', 512).notNullable();
      table.string('url_publica', 512).notNullable();
      table.boolean('es_publico').notNullable().defaultTo(false);
      table.integer('subido_por').unsigned();
      table.timestamps(true, true);
      table.timestamp('deleted_at');
    });

    await knex.schema.alterTable('archivos', (table) => {
      table.index(['es_publico'], 'archivos_es_publico_idx');
      table.index(['deleted_at'], 'archivos_deleted_at_idx');
    });
  }

  const hasNews = await knex.schema.hasTable('noticias');
  if (!hasNews) {
    await knex.schema.createTable('noticias', (table) => {
      table.increments('id').primary();
      table.string('titulo', 255).notNullable();
      table.text('contenido');
      table.string('estado', 50).notNullable().defaultTo('draft');
      table.boolean('requiere_confirmacion').notNullable().defaultTo(false);
      table.timestamp('publicado_desde');
      table.timestamp('publicado_hasta');
      table.text('audiencia_roles');
      table.text('audiencia_cursos');
      table.text('audiencia_usuarios');
      table.string('portada_url', 512);
      table.string('portada_path', 512);
      table.integer('creado_por').unsigned();
      table.integer('actualizado_por').unsigned();
      table.timestamps(true, true);
      table.timestamp('archived_at');
    });

    await knex.schema.alterTable('noticias', (table) => {
      table.index(['estado'], 'noticias_estado_idx');
      table.index(['archived_at'], 'noticias_archived_at_idx');
      table.index(['publicado_desde'], 'noticias_publicado_desde_idx');
    });
  }

  const hasGallery = await knex.schema.hasTable('imagenes');
  if (!hasGallery) {
    await knex.schema.createTable('imagenes', (table) => {
      table.increments('id').primary();
      table.string('categoria', 120).notNullable();
      table.string('archivo', 255).notNullable();
      table.string('archivo_url', 512).notNullable();
      table.string('archivo_path', 512);
      table.string('autor', 255).notNullable();
      table.text('descripcion').notNullable();
      table.integer('subido_por').unsigned();
      table.timestamp('fecha_subida').defaultTo(knex.fn.now());
    });

    await knex.schema.alterTable('imagenes', (table) => {
      table.index(['categoria'], 'imagenes_categoria_idx');
      table.index(['fecha_subida'], 'imagenes_fecha_subida_idx');
    });
  }
}

export async function down(knex) {
  const tables = ['imagenes', 'noticias', 'archivos'];

  for (const table of tables) {
    const exists = await knex.schema.hasTable(table);
    if (exists) {
      await knex.schema.dropTable(table);
    }
  }
}
